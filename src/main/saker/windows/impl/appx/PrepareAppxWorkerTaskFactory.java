package saker.windows.impl.appx;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import saker.build.file.DelegateSakerFile;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities;
import saker.build.task.TaskFactory;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.trace.BuildTrace;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.windows.main.appx.PrepareAppxTaskFactory;

public class PrepareAppxWorkerTaskFactory implements TaskFactory<Object>, Task<Object>, Externalizable {
	private static final long serialVersionUID = 1L;

	private NavigableMap<SakerPath, FileLocation> resources;

	/**
	 * For {@link Externalizable}.
	 */
	public PrepareAppxWorkerTaskFactory() {
	}

	public PrepareAppxWorkerTaskFactory(NavigableMap<SakerPath, FileLocation> resources) {
		this.resources = resources;
	}

	@Override
	public Object run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
		}
		taskcontext.setStandardOutDisplayIdentifier(PrepareAppxTaskFactory.TASK_NAME);

		PrepareAppxWorkerTaskIdentifier taskid = (PrepareAppxWorkerTaskIdentifier) taskcontext.getTaskId();
		TaskExecutionUtilities taskutils = taskcontext.getTaskUtilities();
		SakerDirectory outputdir = taskutils.resolveDirectoryAtRelativePathCreate(
				SakerPathFiles.requireBuildDirectory(taskcontext).getDirectoryCreate(PrepareAppxTaskFactory.TASK_NAME),
				taskid.getRelativeOutput());

		//TODO don't clear the directory, but only remove previous outputs. this is so windows or visual studio metadata is preserved
		outputdir.clear();
		SakerPath outputdirsakerpath = outputdir.getSakerPath();

		NavigableMap<SakerPath, ContentDescriptor> inputdependencies = new TreeMap<>();
		NavigableMap<SakerPath, ContentDescriptor> outputdependencies = new TreeMap<>();
		NavigableMap<SakerPath, SakerPath> mappingpaths = new TreeMap<>();
		for (Entry<SakerPath, FileLocation> entry : resources.entrySet()) {
			SakerPath outpath = entry.getKey();
			SakerDirectory parentdir = taskutils.resolveDirectoryAtRelativePathCreate(outputdir, outpath.getParent());
			entry.getValue().accept(new FileLocationVisitor() {
				@Override
				public void visit(ExecutionFileLocation loc) {
					SakerPath locpath = loc.getPath();
					SakerFile f = taskutils.resolveAtPath(locpath);
					if (f == null) {
						throw ObjectUtils.sneakyThrow(new FileNotFoundException(locpath.toString()));
					}
					if (f instanceof SakerDirectory) {
						//directories are ignored
						//makeappx.exe reports:
						//   MakeAppx : error: You can't add folders or devices to the package: ...
						return;
					}
					ContentDescriptor cd = f.getContentDescriptor();
					inputdependencies.put(locpath, cd);
					SakerPath outpath = parentdir.getSakerPath().resolve(f.getName());
					outputdependencies.put(outpath, cd);

					mappingpaths.put(outpath, outputdirsakerpath.relativize(outpath));

					if (f instanceof SakerDirectory) {
						parentdir.getDirectoryCreate(outpath.getFileName());
						return;
					}
					DelegateSakerFile addefile = new DelegateSakerFile(outpath.getFileName(), f);
					parentdir.add(addefile);
				}
			});
		}

		outputdir.synchronize();
		taskutils.reportOutputFileDependency(null, outputdependencies);
		taskutils.reportInputFileDependency(null, inputdependencies);

		PrepareAppxWorkerTaskOutputImpl result = new PrepareAppxWorkerTaskOutputImpl(outputdir.getSakerPath(),
				ImmutableUtils.makeImmutableNavigableMap(mappingpaths));
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
		return result;
	}

	@Override
	public Task<? extends Object> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalMap(out, resources);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		resources = SerialUtils.readExternalSortedImmutableNavigableMap(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resources == null) ? 0 : resources.hashCode());
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
		PrepareAppxWorkerTaskFactory other = (PrepareAppxWorkerTaskFactory) obj;
		if (resources == null) {
			if (other.resources != null)
				return false;
		} else if (!resources.equals(other.resources))
			return false;
		return true;
	}

}
