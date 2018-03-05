/* 

class Files provides file utilities including copyFile() and replaceFile()

note: these files utilities are binary safe

used by Search.java 

IMPORTANT NOTICE, please read:

This software is licensed under the terms of the GNU GENERAL PUBLIC LICENSE,
please read the enclosed file license.txt or http://www.gnu.org/licenses/licenses.html

Note that this software is freeware and it is not designed, licensed or intended
for use in mission critical, life support and military purposes.

The use of this software is at the risk of the user.
*/

package search;
import java.io.*;

public class Files {
	
	public static int copy(
            InputStream input,
            OutputStream output)
                throws IOException {
        byte[] buffer = new byte[2048];
        int n, count = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static void copyFile(String source, String destination)
                throws IOException {
		copyFile(new File(source), new File(destination));
	}


    public static void copyFile(File source, File destination)
                throws IOException {
        //check source exists
        if (!source.exists()) {
            String message = "File " + source + " does not exist";
            throw new FileNotFoundException(message);
        }

        //does destinations directory exist ?
        if (destination.getParentFile() != null
            && !destination.getParentFile().exists()) {
            destination.getParentFile().mkdirs();
        }

        //make sure we can write to destination
        if (destination.exists() && !destination.canWrite()) {
            throw new IOException("Unable to open file " + destination + " for writing.");
        }

        //makes sure it is not the same file        
        if (source.getCanonicalPath().equals(destination.getCanonicalPath())) {
            throw new IOException("Unable to write file " + source + " on itself.");
        }

        FileInputStream input = new FileInputStream(source);
		FileOutputStream output;
		if (destination.isDirectory()) 
			output = new FileOutputStream(new File(destination.getPath()+File.separatorChar+source.getName()));
		else output = new FileOutputStream(destination);
        if (source.length() != copy(input, output)) {
            throw new IOException("Failed to copy full contents from " + source + " to " + destination);
        }
 		input.close();
		output.close();
       
        destination.setLastModified(source.lastModified());      
    }

	public static void replaceFile(String fname, String searchStr, String replaceStr) throws IOException {
		if (searchStr.length() == 0) throw new IOException("replaceFile: search string cannot be empty");

		File f = File.createTempFile("temp",null,new File("."));

        FileInputStream input = new FileInputStream(fname);
        FileOutputStream output = new FileOutputStream(f);
		byte[] buffer = new byte[2048];
		byte[] search = searchStr.getBytes(); int seacount = search.length;
		byte[] replace = replaceStr.getBytes(); int repcount = replace.length;
        int nread, readoffset = 0;
        while (-1 != (nread = input.read(buffer, readoffset, 2048-readoffset))) {
			int n = nread + readoffset;
			int offset = 0;
			int fromIndex = 0;
			byte first  = search[0];
			int max = n - seacount;
			boolean next;	
			do {
				next = false;
				for (int i = fromIndex; i <= max; i++) {
					/* Look for first character. */
					if (buffer[i] != first)
						while (++i <= max && buffer[i] != first);

					/* Found first character, now look at the rest of search string */
					if (i <= max) {
						int j = i + 1;
						int end = i + seacount;
						for (int k = 1; j < end && buffer[j] == search[k]; j++, k++);

						if (j == end) {
							/* Found search string -> do replace*/
							output.write(buffer, offset, i-offset);
							output.write(replace, 0, repcount);
							offset = i+seacount;
							if (end <= max)
								next = true;
							fromIndex = end;
						}
					}
				}
			} while (next);

			if (n-offset > seacount-1) {
				readoffset = seacount-1;
			} else {
				readoffset = n-offset;
			}
			output.write(buffer, offset, n-offset-readoffset);
			if (readoffset > 0)
				System.arraycopy(buffer, 0 + n - readoffset, buffer, 0, readoffset);
        }
		if (readoffset > 0)
			output.write(buffer, 0, readoffset);
 		input.close();
		output.close();

		new File(fname+".bak").delete();
		new File(fname).renameTo(new File(fname+".bak"));
		f.renameTo(new File(fname));
	} // end of replaceFile()



}

