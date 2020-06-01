package saker.windows.main.appx;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.TaskResultResolver;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.StructuredTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.trace.BuildTrace;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.api.util.SakerStandardTaskUtils;
import saker.windows.api.appx.PrepareAppxWorkerTaskOutput;
import saker.windows.impl.appx.RegisterAppxWorkerTaskFactory;

public class RegisterAppxTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.appx.register";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Appx" }, required = true)
			public AppxReferenceTaskOption appxReferenceOption;

			@SakerInput(value = "AllowReinstall")
			public boolean allowReinstall = false;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}

				FileLocation appx = appxReferenceOption.getAppxManifest();

				StructuredTaskResult[] appxpathtaskresult = { null };
				appx.accept(new FileLocationVisitor() {
					@Override
					public void visit(LocalFileLocation loc) {
						appxpathtaskresult[0] = StructuredTaskResult.createLiteral(loc.getLocalPath());
					}

					@Override
					public void visit(ExecutionFileLocation loc) {
						SakerPath locpath = loc.getPath();
						SakerPath locparentpath = locpath.getParent();
						TaskFactory<? extends SakerPath> mirrortf = SakerStandardTaskUtils
								.createMirroringTaskFactory(locparentpath);
						TaskIdentifier mirrortaskid = SakerStandardTaskUtils
								.createMirroringTaskIdentifier(locparentpath);
						taskcontext.startTask(mirrortaskid, mirrortf, null);

						String appxfilename = locpath.getFileName();
						appxpathtaskresult[0] = new FileNameResolvingStructuredTaskResult(appxfilename, mirrortaskid);
					}

				});

				RegisterAppxWorkerTaskFactory workertask = new RegisterAppxWorkerTaskFactory(appxpathtaskresult[0]);
				workertask.setAllowReinstall(allowReinstall);
				taskcontext.startTask(workertask, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertask);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

	private static final class FileNameResolvingStructuredTaskResult implements StructuredTaskResult, Externalizable {
		private static final long serialVersionUID = 1L;

		private String appxManifestFileName;
		private TaskIdentifier mirrorTaskId;

		/**
		 * For {@link Externalizable}.
		 */
		public FileNameResolvingStructuredTaskResult() {
		}

		private FileNameResolvingStructuredTaskResult(String appxfilename, TaskIdentifier mirrortaskid) {
			this.appxManifestFileName = appxfilename;
			this.mirrorTaskId = mirrortaskid;
		}

		@Override
		public Object toResult(TaskResultResolver results) throws NullPointerException, RuntimeException {
			SakerPath mirrorres = (SakerPath) results.getTaskResult(mirrorTaskId);
			return mirrorres.resolve(appxManifestFileName);
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(appxManifestFileName);
			out.writeObject(mirrorTaskId);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			appxManifestFileName = SerialUtils.readExternalObject(in);
			mirrorTaskId = SerialUtils.readExternalObject(in);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((appxManifestFileName == null) ? 0 : appxManifestFileName.hashCode());
			result = prime * result + ((mirrorTaskId == null) ? 0 : mirrorTaskId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FileNameResolvingStructuredTaskResult other = (FileNameResolvingStructuredTaskResult) obj;
			if (appxManifestFileName == null) {
				if (other.appxManifestFileName != null)
					return false;
			} else if (!appxManifestFileName.equals(other.appxManifestFileName))
				return false;
			if (mirrorTaskId == null) {
				if (other.mirrorTaskId != null)
					return false;
			} else if (!mirrorTaskId.equals(other.mirrorTaskId))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "FileNameResolvingStructuredTaskResult["
					+ (appxManifestFileName != null ? "appxManifestFileName=" + appxManifestFileName + ", " : "")
					+ (mirrorTaskId != null ? "mirrorTaskId=" + mirrorTaskId : "") + "]";
		}

	}

	public static abstract class AppxReferenceTaskOption {
		public abstract FileLocation getAppxManifest();

		public static AppxReferenceTaskOption valueOf(PrepareAppxWorkerTaskOutput value) {
			return new AppxReferenceTaskOption() {
				@Override
				public FileLocation getAppxManifest() {
					return ExecutionFileLocation.create(value.getAppxDirectory().resolve("AppxManifest.xml"));
				}
			};
		}
	}
}
