package saker.windows.main.appx;

import java.util.Map;
import java.util.NavigableMap;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.trace.BuildTrace;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.main.SDKSupportFrontendUtils;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;
import saker.std.api.file.location.LocalFileLocation;
import saker.windows.api.SakerWindowsUtils;
import saker.windows.api.appx.RegisterAppxWorkerTaskOutput;
import saker.windows.impl.appx.DynamicLaunchAppxWorkerTaskFactory;
import saker.windows.impl.appx.InstallLocationLaunchAppxWorkerTaskFactory;
import saker.windows.impl.appx.LaunchAppxWorkerTaskFactoryBase;
import saker.windows.impl.appx.SimpleLaunchAppxWorkerTaskFactory;
import saker.windows.main.TaskDocs;

@NestInformation("Launches an appx application.\n"
		+ "The task can be used to start an UWP or other applications. Generally it is used to "
		+ "start the application you're developing.\n"
		+ "The task will use the appxlauncher tool to start the application. It requires the AUMID (Application "
		+ "User Model ID) of the app to start it. The AUMID consists of the FamilyName and AppID in the following format:\n"
		+ "FamilyName!AppID\n"
		+ "The task will attempt to determine this automatically, but in some cases you may need to specify it explicitly.")

@NestParameterInformation(value = "Input",
		aliases = "",
		required = true,
		type = @NestTypeUsage(LaunchAppxTaskFactory.LaunchAppxInputTaskOption.class),
		info = @NestInformation("Specifies the input application to launch.\n" + "It can be the output of the "
				+ RegisterAppxTaskFactory.TASK_NAME + "() task, a local file location for "
				+ "an AppxManifest.xml or application directory, package name or install location of the application."))
@NestParameterInformation(value = "ApplicationId",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("The application ID to launch.\n"
				+ "This is the value of the Id attribute in the Package/Applications/Application element in the "
				+ "AppxManifest.xml file.\n"
				+ "Most of the time the task can automatically determine this, however, in some cases you may need "
				+ "to explicitly specify it."))
@NestParameterInformation(value = "SDKs",
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { saker.sdk.support.main.TaskDocs.DocSdkNameOption.class,
						SDKDescriptionTaskOption.class }),
		info = @NestInformation(TaskDocs.SDKS))
public class LaunchAppxTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.appx.launch";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new TaskImpl();
	}

	private static final class TaskImpl implements ParameterizableTask<Object> {
		@SakerInput(value = { "", "Input" }, required = true)
		public LaunchAppxInputTaskOption inputOption;

		@SakerInput(value = "ApplicationId")
		public String applicationId;

		@SakerInput(value = { "SDKs" })
		public Map<String, SDKDescriptionTaskOption> sdksOption;

		@Override
		public Object run(TaskContext taskcontext) throws Exception {
			if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
				BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
			}

			NavigableMap<String, SDKDescription> sdks = SDKSupportFrontendUtils.toSDKDescriptionMap(sdksOption);
			sdks.putIfAbsent(SakerWindowsUtils.SDK_NAME_WINDOWSAPPCERTKIT,
					SakerWindowsUtils.getDefaultWindowsAppCertKitSDK());

			LaunchAppxWorkerTaskFactoryBase launcher = inputOption.createTask(taskcontext, this);
			launcher.setSdks(sdks);
			taskcontext.startTask(launcher, launcher, null);
			SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(launcher);
			taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
			return result;
		}
	}

	@NestInformation("Input appx to launch.\n" + "The option accepts the output of the "
			+ RegisterAppxTaskFactory.TASK_NAME
			+ "() task as well as a local file location to an AppxManifest.xml for the application.\n"
			+ "An Application User Model ID can also be used to specify the application to start.")
	public static abstract class LaunchAppxInputTaskOption {
		public abstract LaunchAppxWorkerTaskFactoryBase createTask(TaskContext taskcontext, TaskImpl task);

		public static LaunchAppxInputTaskOption valueOf(String input) {
			return new LaunchAppxInputTaskOption() {
				@Override
				public LaunchAppxWorkerTaskFactoryBase createTask(TaskContext taskcontext, TaskImpl task) {
					if (input.indexOf('!') > 0) {
						//it is assumed to be an AUMID
						if (task.applicationId != null) {
							throw new IllegalArgumentException(
									"Input Application User Model ID: " + input + " already contains ApplicationId.");
						}
						return new SimpleLaunchAppxWorkerTaskFactory(input);
					}
					DynamicLaunchAppxWorkerTaskFactory result = new DynamicLaunchAppxWorkerTaskFactory(input);
					result.setApplicationId(task.applicationId);
					return result;
				}
			};
		}

		public static LaunchAppxInputTaskOption valueOf(RegisterAppxWorkerTaskOutput input) {
			return new LaunchAppxInputTaskOption() {
				@Override
				public LaunchAppxWorkerTaskFactoryBase createTask(TaskContext taskcontext, TaskImpl task) {
					InstallLocationLaunchAppxWorkerTaskFactory result = new InstallLocationLaunchAppxWorkerTaskFactory(
							input.getAppxManifestLocalPath().getParent());
					result.setApplicationId(task.applicationId);
					return result;
				}
			};
		}

		public static LaunchAppxInputTaskOption valueOf(LocalFileLocation input) {
			return new LaunchAppxInputTaskOption() {
				@Override
				public LaunchAppxWorkerTaskFactoryBase createTask(TaskContext taskcontext, TaskImpl task) {
					SakerPath path = input.getLocalPath();
					if ("AppxManifest.xml".equalsIgnoreCase(path.getFileName())) {
						path = path.getParent();
					}
					InstallLocationLaunchAppxWorkerTaskFactory result = new InstallLocationLaunchAppxWorkerTaskFactory(
							path);
					result.setApplicationId(task.applicationId);
					return result;
				}
			};
		}
	}
}
