package net.guarnie.tspdemo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StreamTokenizer;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Questa applicazione (separata da tspdemo) genera un file index.zip contenente
 * index.txt, ovvero l'elenco dei files della directory, il cui nome deve essere 
 * passato come parametro. 
 * Se questi rappresentano istanze o circuiti della TSPLIB'95 vengono inserire 
 * maggiori informazioni rispetto al solo nome del file.
 */
class MakeIndex {
	static String comment;
	static int instanceDim;
	static String instanceType;

	public static void main(String[] args) {
		if ((args.length > 0) && (args[0] != null)) {
			String path = args[0];
			File f = new File(path, "");
			String[] fileList = f.list();
			
			if ((fileList == null) || (fileList.length == 0)) {
				System.out.println("Error: " + path + " not exist or is empty.");
				System.exit(1);
			}

			Arrays.sort(fileList);

			try 
			(
				ZipOutputStream outStream = new ZipOutputStream(new FileOutputStream("index.zip"));
				BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(outStream));
			)
			{
				String optName;
				int pos;
				outStream.putNextEntry(new ZipEntry("index.txt"));
				String str = "FILES: " + String.valueOf(fileList.length);
				outWriter.write(str, 0, str.length());
				outWriter.newLine();
				// Scorre la lista di files nel path
				for (int count = 0; count < fileList.length; count++) {
					f = new File(path, fileList[count]);
					if (f.isFile())
					{
						instanceType = null;
						comment = null;
						instanceDim = -1;

						loadInstanceInfo(f);

						outWriter.newLine();
						str = "NAME: " + fileList[count];
						outWriter.write(str, 0, str.length());
						outWriter.newLine();
			
						str = "TYPE: ";
						if (instanceType != null)
							str += instanceType;
						outWriter.write(str, 0, str.length());
						outWriter.newLine();
						str = "DESCRIPTION: ";
						if (comment != null)
							str += comment;
						outWriter.write(str, 0, str.length());
						outWriter.newLine();
						str = "OPTIMUM: ";
						optName = f.getName();
						pos = optName.lastIndexOf(".tsp");
						if (pos != -1) {
							optName = optName.substring(0, pos) + ".opt.tour";
							f = new File(path, optName);
							if (f.exists())
								str += optName;
						}
						outWriter.write(str, 0, str.length());
						outWriter.newLine();
					}
				}
				
			} catch (IOException ioe) {
				System.out.println("I/O Error: " + ioe.getMessage());
				System.exit(1);
			}
		} 
		else
		{	
			System.out.println("MakeIndex <TSPLIB95 Instances directory path>");
			System.out.println("Make index.zip that contains file index.txt of TSPLIB95 instances");
			System.exit(1);
		}
	}

	/**
	 * Preleva le informazioni a riguardo del corrente file se e' nel formato
	 * della TSPLIB'95.
	 */
	private static void loadInstanceInfo(File file) {
		String tokenStr;
		try 
		(
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		)
		{
			StreamTokenizer inToken = new StreamTokenizer(inputReader);
			inToken.wordChars('!', '*');
			inToken.wordChars('/', '/');
			inToken.whitespaceChars(':', ':');
			inToken.wordChars(';', '@');
			inToken.wordChars('[', '`');
			inToken.wordChars('{', '~');
			boolean canStop = false;
			do {
				inToken.nextToken();
				switch (inToken.ttype) {
					case StreamTokenizer.TT_EOF:
						break;
					case StreamTokenizer.TT_WORD:
						tokenStr = inToken.sval;
						//
						// THE SPECIFICATION PART
						//
						switch (tokenStr)
						{
							case "NAME":
								inputReader.readLine();
								break;
							case "TYPE":
								inToken.nextToken();
								instanceType = inToken.sval;
								break;
							case "COMMENT":
								comment = inputReader.readLine();
								break;
							case "DIMENSION":
								inToken.nextToken();
								instanceDim = (int) inToken.nval;
								break;
							case "EDGE_WEIGHT_TYPE":
								inToken.nextToken();
								break;
							case "EDGE_WEIGHT_FORMAT":
								inToken.nextToken();
								break;
							case "EDGE_DATA_FORMAT":
								inToken.nextToken();
								break;
							case "NODE_COORD_TYPE":
								inToken.nextToken();
								break;
							case "DISPLAY_DATA_TYPE":
								inToken.nextToken();
								break;
						}
						break;
					default:
						canStop = true;
				}
			} while (!canStop && inToken.ttype != StreamTokenizer.TT_EOF);
		} catch (IOException ioe) {
			System.out.println("I/O Error: " + ioe.getMessage());
		}
	}
}