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
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.main.SDKSupportFrontendUtils;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;
import saker.std.api.file.location.LocalFileLocation;
import saker.windows.api.SakerWindowsUtils;
import saker.windows.api.appx.RegisterAppxWorkerTaskOutput;
import saker.windows.impl.appx.DynamicLaunchAppxWorkerTaskFactory;
import saker.windows.impl.appx.InstallLocationLaunchAppxWorkerTaskFactory;
import saker.windows.impl.appx.LaunchAppxWorkerTaskFactory;
import saker.windows.impl.appx.SimpleLaunchAppxWorkerTaskFactory;

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

			LaunchAppxWorkerTaskFactory launcher = inputOption.createTask(taskcontext, this);
			launcher.setSdks(sdks);
			taskcontext.startTask(launcher, launcher, null);
			SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(launcher);
			taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
			return result;
		}
	}

	public static abstract class LaunchAppxInputTaskOption {
		public abstract LaunchAppxWorkerTaskFactory createTask(TaskContext taskcontext, TaskImpl task);

		public static LaunchAppxInputTaskOption valueOf(String input) {
			return new LaunchAppxInputTaskOption() {
				@Override
				public LaunchAppxWorkerTaskFactory createTask(TaskContext taskcontext, TaskImpl task) {
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
				public LaunchAppxWorkerTaskFactory createTask(TaskContext taskcontext, TaskImpl task) {
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
				public LaunchAppxWorkerTaskFactory createTask(TaskContext taskcontext, TaskImpl task) {
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
