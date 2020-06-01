package saker.windows.impl.appx;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.SakerFileBase;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;

public class MappingsSakerFile extends SakerFileBase {
	private static final byte[] BRACKET_FILES_BYTES = "[Files]".getBytes();
	private static final byte[] QUOT_TAB_QUOT_BYTES = "\"\t\"".getBytes(StandardCharsets.UTF_8);

	private MappingsContentDescriptor contents;

	public MappingsSakerFile(String name, MappingsContentDescriptor contents)
			throws NullPointerException, InvalidPathFormatException {
		super(name);
		this.contents = contents;
	}

	@Override
	public ContentDescriptor getContentDescriptor() {
		return contents;
	}

	@Override
	public void writeToStreamImpl(OutputStream os) throws IOException, NullPointerException {
		byte[] ls = System.lineSeparator().getBytes(StandardCharsets.UTF_8);
		os.write(BRACKET_FILES_BYTES);
		os.write(ls);

		for (Entry<SakerPath, SakerPath> entry : contents.getMappings().entrySet()) {
			os.write('"');
			os.write(entry.getKey().toString().getBytes(StandardCharsets.UTF_8));
			os.write(QUOT_TAB_QUOT_BYTES);
			os.write(entry.getValue().toString().getBytes(StandardCharsets.UTF_8));
			os.write('"');
			os.write(ls);
		}
	}

}