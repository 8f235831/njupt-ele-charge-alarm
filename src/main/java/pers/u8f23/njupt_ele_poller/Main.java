package pers.u8f23.njupt_ele_poller;

import com.google.gson.Gson;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.extern.slf4j.Slf4j;
import pers.u8f23.njupt_ele_poller.entity.config.RequestItem;
import pers.u8f23.njupt_ele_poller.entity.config.RootConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 入口主函数。
 *
 * @author 8f23
 * @create 2023/4/15-11:57
 */
@Slf4j
public class Main
{
	public static void main(String[] args)
	{
		RxJavaPlugins.setErrorHandler((th) -> log.info(
			"global rxjava error " + "catch: ",
			th
		));
		String definedConfigFile = args.length > 0 ? args[0] : null;
		if (definedConfigFile == null)
		{
			log.info("use default config file.");
		}
		else
		{
			log.info("use assigned config file at\"{}\".", definedConfigFile);
		}
		RootConfig config = readConfig(definedConfigFile);
		if (config == null)
		{
			log.error("missing config file!");
			return;
		}
		RootConfig.initReference(config);
		log.info("Success to load config:{}", new Gson().toJson(config));

		// keep alive if assigned.
		boolean isKeepAlive = args.length > 1;
		if (isKeepAlive)
		{
			log.info("try keep alive.");
			keepAliveTry();
			log.info("success to try keep alive.");
			return;
		}

		// request.
		Collection<Completable> tasks = config.getRequests()
			.stream()
			.map(RequestItem::asRequest)
			.collect(Collectors.toSet());
		log.info("requesting results.");
		Completable.concat(tasks)
			.blockingSubscribe();
		log.info("linking results.");
		Map<String, String> linkedResult = config.linkResults();
		log.info("sending mails.");
		for (Map.Entry<String, String> e : linkedResult.entrySet())
		{
			try
			{
				sendMail(e.getKey(), e.getValue());
			}
			catch (Throwable th)
			{
				log.error("failed to send mail to {}", e.getKey(), th);
			}
		}
		log.info("success to send mails.");

		// write
		log.info("writing request logs.");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("log date: ")
			.append(new Date())
			.append("\n");
		config.getRequests()
			.forEach(item -> {
				String name = item.getName();
				String msg = item.getResponseMsg();
				msg = msg == null ? "请求失败" : msg;
				logBuilder.append(name)
					.append(": ")
					.append(msg)
					.append("\n");
			});
		logBuilder.append("\n");
		try
		{
			appendToFile(config.getFormalLog(), logBuilder.toString());
		}
		catch (Exception e)
		{
			log.error("failed to log.", e);
		}
		log.info("success to finish ");
	}

	private static RootConfig readConfig(String path)
	{
		try
		{
			File file = new File((path == null || path.isEmpty())
				? "config.json"
				: path);
			String configJson = new String(Files.readAllBytes(file.toPath()));
			return new Gson().fromJson(configJson, RootConfig.class);
		}
		catch (Exception e)
		{
			log.error("failed to read config. ", e);
			return null;
		}
	}

	private static void sendMail(String receiver, String content)
		throws MessagingException
	{
		RootConfig config = RootConfig.getReference();
		String emailHost = Objects.requireNonNull(config.getEmailHost());
		String transportType =
			Objects.requireNonNull(config.getTransportType());
		String fromUser = Objects.requireNonNull(config.getFromEmail());
		String fromEmail = Objects.requireNonNull(config.getFromEmail());
		String authCode = Objects.requireNonNull(config.getAuthCode());
		Properties props = new Properties();
		props.setProperty(
			"mail.transport.protocol",
			transportType
		);
		props.setProperty("mail.host", emailHost);
		props.setProperty("mail.user", fromUser);
		props.setProperty("mail.from", fromEmail);
		Session session = Session.getInstance(props, null);
		// session.setDebug(true);
		MimeMessage message = new MimeMessage(session);
		InternetAddress from = new InternetAddress(fromEmail);
		message.setFrom(from);
		InternetAddress to = new InternetAddress(receiver);
		message.setRecipient(Message.RecipientType.TO, to);
		message.setSubject("[电费] 当前电费余额查询结果");
		Multipart multipart = new MimeMultipart();
		BodyPart textPart = new MimeBodyPart();
		textPart.setContent(content, "text/html;charset=utf8");
		multipart.addBodyPart(textPart);
		message.setContent(multipart);
		message.saveChanges();
		Transport transport = session.getTransport();
		transport.connect(emailHost, fromEmail, authCode);
		transport.sendMessage(message, message.getAllRecipients());
		log.info("success to send mail to \"{}\"", to);
	}

	private static void keepAliveTry()
	{
		RootConfig config = RootConfig.getReference();
		Map<String, String> body = RootConfig
			.getReference()
			.getRequests()
			.stream()
			.findAny()
			.orElseThrow()
			.getRequestBody();
		HttpUtils.buildService(Service.class)
			.queryEleRoomInfo(body)
			.subscribeOn(Schedulers.trampoline())
			.observeOn(Schedulers.trampoline())
			.map(res -> Objects.requireNonNull(res.body()).getMsg())
			.doOnSuccess(msg -> log.info("keep alive response msg: {}", msg))
			.doOnSuccess(msg -> appendToFile(
				config.getKeepAliveLog(),
				new Date() + ": " + msg + "\n"
			))
			.blockingSubscribe();
	}

	private static void appendToFile(String filePath, String content) throws IOException
	{
		try (FileOutputStream fos = new FileOutputStream(filePath, true))
		{
			// SuppressWarnings all
			fos.write(content.getBytes(StandardCharsets.UTF_8));
		}
	}
}
