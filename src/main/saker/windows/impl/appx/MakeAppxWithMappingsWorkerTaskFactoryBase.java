package saker.windows.impl.appx;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import saker.build.file.DirectoryVisitPredicate;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities;
import saker.build.task.TaskFactory;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ArrayUtils;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.trace.BuildTrace;
import saker.process.api.CollectingProcessIOConsumer;
import saker.process.api.SakerProcess;
import saker.process.api.SakerProcessBuilder;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.SDKSupportUtils;
import saker.sdk.support.api.exc.SDKPathNotFoundException;
import saker.windows.api.SakerWindowsUtils;

public abstract class MakeAppxWithMappingsWorkerTaskFactoryBase<T> implements TaskFactory<T>, Task<T>, Externalizable {
	private static final long serialVersionUID = 1L;

	private NavigableMap<SakerPath, SakerPath> mappings;

	private NavigableMap<String, SDKDescription> sdks;

	/**
	 * For {@link Externalizable}.
	 */
	public MakeAppxWithMappingsWorkerTaskFactoryBase() {
	}

	public MakeAppxWithMappingsWorkerTaskFactoryBase(NavigableMap<SakerPath, SakerPath> mappings) {
		this.mappings = mappings;
	}

	public void setSdks(NavigableMap<String, SDKDescription> sdks) {
		this.sdks = ImmutableUtils.makeImmutableNavigableMap(sdks);
	}

	@Override
	public T run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
		}

		SakerPath relativeoutputpath = getRelativeOutputPath(taskcontext);
		TaskExecutionUtilities taskutils = taskcontext.getTaskUtilities();
		SakerDirectory outputdir = taskutils.resolveDirectoryAtRelativePathCreate(
				SakerPathFiles.requireBuildDirectory(taskcontext), relativeoutputpath.getParent());

		NavigableMap<SakerPath, SakerPath> mirroredmappings = new TreeMap<>();
		NavigableMap<SakerPath, ContentDescriptor> inputcontents = new TreeMap<>();
		//TODO this should be done more efficiently in bulk
		for (Entry<SakerPath, SakerPath> entry : mappings.entrySet()) {
			SakerPath fpath = entry.getValue();
			SakerFile f = taskutils.resolveAtPath(fpath);
			if (f == null) {
				throw new FileNotFoundException(fpath.toString());
			}
			Path mirroredpath = taskcontext.mirror(f, DirectoryVisitPredicate.synchronizeNothing());
			mirroredmappings.put(SakerPath.valueOf(mirroredpath), entry.getKey());
			inputcontents.put(fpath, f.getContentDescriptor());
		}
		taskutils.reportInputFileDependency(null, inputcontents);

		String outputappxfilename = relativeoutputpath.getFileName();
		MappingsSakerFile mappingsfile = new MappingsSakerFile(outputappxfilename + ".mappings",
				new MappingsContentDescriptor(mirroredmappings));
		outputdir.add(mappingsfile);
		//do not report output dependency on the mappings file as we dont care if its deleted or modified.

		Path mappingsfilemirrorpath = taskcontext.mirror(mappingsfile);
		Path outputfilepath = mappingsfilemirrorpath.resolveSibling(outputappxfilename);

		SakerPath makeappxpath = MakeAppxWithMappingsWorkerTaskFactoryBase.getMakeAppxExecutablePath(taskcontext, sdks);

		SakerProcessBuilder pb = SakerProcessBuilder.create();
		pb.setCommand(getCommand(mappingsfilemirrorpath, outputfilepath, makeappxpath));
		pb.setStandardErrorMerge(true);
		CollectingProcessIOConsumer outconsumer = new CollectingProcessIOConsumer();
		pb.setStandardOutputConsumer(outconsumer);
		boolean displayout = false;
		try (SakerProcess proc = pb.start()) {
			proc.processIO();
			int ec = proc.waitFor();
			if (ec != 0) {
				throw new IOException("Failed to run makeappx.exe. Exit code: " + ec);
			}
		} catch (Throwable e) {
			displayout = true;
			throw e;
		} finally {
			//somewhy makeappx.exe produces output with \r\r\n sequences when only a single new line is expected
			//it actually displays correctly in the console, but when the output is redirected, it is incorrect
			//fix these to avoid unnecessary bloating the output
			String outputstr = outconsumer.getOutputString();
			if (!displayout) {
				//not displaying the output.
				//check if there's any warnings, infos, or errors in it
				String lc = outputstr.toLowerCase(Locale.ENGLISH);
				if (lc.contains("warning") || lc.contains("error") || lc.contains("info")) {
					displayout = true;
				}
			}
			if (displayout) {
				taskcontext.getStandardOut().write(
						ByteArrayRegion.wrap(outputstr.replace("\r\r\n", "\r\n").getBytes(StandardCharsets.UTF_8)));
			}
		}

		taskutils.addSynchronizeInvalidatedProviderPathFileToDirectory(outputdir,
				LocalFileProvider.getInstance().getPathKey(outputfilepath), outputappxfilename);
		SakerFile outfile = outputdir.get(outputappxfilename);

		SakerPath outputpath = outfile.getSakerPath();
		taskcontext.reportOutputFileDependency(null, outputpath, outfile.getContentDescriptor());

		T result = getResult(outputpath);
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
		return result;
	}

	protected abstract List<String> getCommand(Path mappingsfilemirrorpath, Path outputfilepath,
			SakerPath makeappxpath);

	protected abstract T getResult(SakerPath outputpath);

	protected abstract SakerPath getRelativeOutputPath(TaskContext taskcontext);

	@Override
	public Task<? extends T> createTask(ExecutionContext executioncontext) {
		return this;
	}

	public static SakerPath getMakeAppxExecutablePath(TaskContext taskcontext,
			NavigableMap<String, SDKDescription> sdks) throws Exception {
		Throwable[] causes = {};
		boolean haswk = sdks.containsKey(SakerWindowsUtils.SDK_NAME_WINDOWSKITS);
		if (haswk) {
			try {
				SDKReference sdk = SDKSupportUtils.requireSDK(SDKSupportUtils.resolveSDKReferences(taskcontext, sdks),
						SakerWindowsUtils.SDK_NAME_WINDOWSKITS);
				return tryGetExecutablePathFromSDK(sdk, SakerWindowsUtils.SDK_WINDOWSKITS_PATH_MAKEAPPX_X86);
			} catch (Exception e) {
				causes = ArrayUtils.appended(causes, e);
			}
		}
		boolean hasuap = sdks.containsKey(SakerWindowsUtils.SDK_NAME_WINDOWSUAP);
		if (hasuap) {
			try {
				SDKReference sdk = SDKSupportUtils.requireSDK(SDKSupportUtils.resolveSDKReferences(taskcontext, sdks),
						SakerWindowsUtils.SDK_NAME_WINDOWSUAP);
				return tryGetExecutablePathFromSDK(sdk, SakerWindowsUtils.SDK_WINDOWSUAP_PATH_MAKEAPPX_X86);
			} catch (Exception e) {
				causes = ArrayUtils.appended(causes, e);
			}
		}
		boolean hasappcertkit = sdks.containsKey(SakerWindowsUtils.SDK_NAME_WINDOWSAPPCERTKIT);
		if (hasappcertkit) {
			try {
				SDKReference sdk = SDKSupportUtils.requireSDK(SDKSupportUtils.resolveSDKReferences(taskcontext, sdks),
						SakerWindowsUtils.SDK_NAME_WINDOWSAPPCERTKIT);
				return tryGetExecutablePathFromSDK(sdk, SakerWindowsUtils.SDK_WINDOWSAPPCERTKIT_PATH_MAKEAPPX);
			} catch (Exception e) {
				causes = ArrayUtils.appended(causes, e);
			}
		}
		if (!haswk) {
			try {
				SDKReference sdk = SDKSupportUtils.resolveSDKReference(taskcontext,
						SakerWindowsUtils.getDefaultWindowsKitsSDK());
				return tryGetExecutablePathFromSDK(sdk, SakerWindowsUtils.SDK_WINDOWSKITS_PATH_MAKEAPPX_X86);
			} catch (Exception e) {
				causes = ArrayUtils.appended(causes, e);
			}
		}
		if (!hasuap) {
			try {
				SDKReference sdk = SDKSupportUtils.resolveSDKReference(taskcontext,
						SakerWindowsUtils.getDefaultWindowsUapSDK());
				return tryGetExecutablePathFromSDK(sdk, SakerWindowsUtils.SDK_WINDOWSUAP_PATH_MAKEAPPX_X86);
			} catch (Exception e) {
				causes = ArrayUtils.appended(causes, e);
			}
		}
		if (!hasappcertkit) {
			try {
				SDKReference sdk = SDKSupportUtils.resolveSDKReference(taskcontext,
						SakerWindowsUtils.getDefaultWindowsAppCertKitSDK());
				return tryGetExecutablePathFromSDK(sdk, SakerWindowsUtils.SDK_WINDOWSAPPCERTKIT_PATH_MAKEAPPX);
			} catch (Exception e) {
				causes = ArrayUtils.appended(causes, e);
			}
		}
		SDKPathNotFoundException exc = new SDKPathNotFoundException("makeappx.exe not found in SDKs.");
		for (Throwable c : causes) {
			exc.addSuppressed(c);
		}
		throw exc;
	}

	private static SakerPath tryGetExecutablePathFromSDK(SDKReference sdk, String pathid)
			throws Exception, IOException {
		SakerPath makeappxpath = sdk.getPath(pathid);
		if (makeappxpath != null && LocalFileProvider.getInstance().getFileAttributes(makeappxpath).isRegularFile()) {
			return makeappxpath;
		}
		throw new SDKPathNotFoundException("Executable not found in SDK: " + sdk + " with path identifier: " + pathid);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalMap(out, sdks);
		SerialUtils.writeExternalMap(out, mappings);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		sdks = SerialUtils.readExternalSortedImmutableNavigableMap(in, SDKSupportUtils.getSDKNameComparator());
		mappings = SerialUtils.readExternalSortedImmutableNavigableMap(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mappings == null) ? 0 : mappings.hashCode());
		result = prime * result + ((sdks == null) ? 0 : sdks.hashCode());
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
		MakeAppxWithMappingsWorkerTaskFactoryBase<?> other = (MakeAppxWithMappingsWorkerTaskFactoryBase<?>) obj;
		if (mappings == null) {
			if (other.mappings != null)
				return false;
		} else if (!mappings.equals(other.mappings))
			return false;
		if (sdks == null) {
			if (other.sdks != null)
				return false;
		} else if (!sdks.equals(other.sdks))
			return false;
		return true;
	}

}
