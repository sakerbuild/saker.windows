package saker.windows.main.sdk;

import java.util.Collection;
import java.util.Set;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.trace.BuildTrace;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;
import saker.windows.api.SakerWindowsUtils;

public class WindowsUapSDKTaskFactory extends FrontendTaskFactory<SDKDescription> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.windows.sdk.uap";

	@Override
	public ParameterizableTask<? extends SDKDescription> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<SDKDescription>() {

			@SakerInput(value = { "", "Version", "Versions" })
			public Collection<String> versionsOption;

			@Override
			public SDKDescription run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_CONFIGURATION);
				}

				Set<String> versions = ImmutableUtils.makeImmutableNavigableSet(versionsOption);
				return SakerWindowsUtils.getWindowsUapSDKForVersions(versions);
			}
		};
	}

}