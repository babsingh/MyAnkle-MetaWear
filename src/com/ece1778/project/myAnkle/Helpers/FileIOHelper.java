package com.ece1778.project.myAnkle.Helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;

import com.ece1778.project.myAnkleUser.BuildConfig;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * Wrapper around 
 * class ExternalStorageHelper and class InternalStorageHelper
 * to interact with
 * External Storage (sdcard, virtual sdcard, accessible by user) and
 * Internal Storage (private to app, hidden from user). */
public class FileIOHelper {
	public final static String EXTERNAL_DIRECTORY_PATH = "/Android/data/com.ece1778.package.iAnkle/myAnkleUser/";
	
	/** External Storage interactions class */
	// Stores files into /Android/data/com.ece1778.package.myAnkle/myAnkleTest/ <folder> / <fileName> . csv
	/** KNOWN BUG: Android MTP doesn't show most recent files (on Nexus 4, so far)
	 * 	Problem: Recently created folders and/or saved files in /Android/data aren't shown on computer (Windows 7).
	 * 	"Fix": Reboot the phone. Then you can navigate through the folder and look at the files.
	 */
	
	public class ExternalStorageHelper {
		private final String TAG = ExternalStorageHelper.class.getSimpleName();
		private Context m_context;
		private String m_path;
		private String m_fileName;
		public File m_file;
		private BufferedReader m_buffReader;
		private BufferedWriter m_buffWriter;
		
		// NOTE: folder must have this format: "myFolder2/.../myFolder0/"
		//		 fileName must have this format: "myFileName.csv"
		public ExternalStorageHelper(Context context, String folder, String fileName) {
			m_context = context;
			m_fileName = fileName;
			if(checkExternalStorageState()) {
				if(folder.contains(EXTERNAL_DIRECTORY_PATH)) {
					// Assume the full path is given, don't add to it
					m_path = folder;
				} else {
					m_path = Environment.getExternalStorageDirectory().toString();
					m_path += EXTERNAL_DIRECTORY_PATH + folder;
				}
				m_file = new File(m_path + m_fileName);
			} else {
				Toast.makeText(m_context, "ExternalStorageState not accessible. USB mass storage mounted?", Toast.LENGTH_LONG).show();
			}
		}
		
		// Checks if m_file exists in External Storage
		// Note: When this class is used for writing files, this method should be called AFTER makeWriteFile() 
		public boolean fileExists() {
			return FileIOHelper.exists(m_file);
		}
		
		public Vector<File> getAllFiles(){
			
			Vector<File> fileList = new Vector<File>();
			
			File dir = new File(m_path);
				for(File f : dir.listFiles()){
					if(f.isFile()){
						fileList.add(f);
					}
				}
				
			return fileList;		
		}
		
		
		
		// Initializes m_buffReader, call BEFORE any read action
		public void makeReadFile() {
			try {
				m_buffReader = new BufferedReader(new FileReader(m_file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Toast.makeText(m_context, "makeReadFile(), Can't find the file named: " + m_fileName, Toast.LENGTH_LONG).show();
			}
		}
		
		// Initializes m_buffReader and creates necessary directories to m_file, call BEFORE any write action		
		public void makeWriteFile() {
			File f = new File(m_path);
			if(!f.exists()) {
				f.mkdirs();
			}
			if(!fileExists()) {
				try {
					if(m_file.createNewFile()) {
						m_buffWriter = new BufferedWriter(new FileWriter(m_file));
					} else {
						if(BuildConfig.DEBUG) Log.e(TAG, "Can't create the file named: " + m_fileName);
						Toast.makeText(m_context, "makeWriteFile(), Can't create the file named: " + m_fileName, Toast.LENGTH_LONG).show();
					}
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(m_context, "makeWriteFile(), FileWriter not created successfully. " + m_fileName, Toast.LENGTH_LONG).show();
				}
			}
		}
		
		// Reads a file from Internal Storage
		// Returns an ArrayList<String> with each line[i] of file (ending with '\n') as an element at index i. 
		// NOTE: Must be called AFTER makeReadFile() to initialize m_buffReader
		public ArrayList<String> readFromFile() {
			ArrayList<String> stringAL = new ArrayList<String>();
			assert(fileExists());
		    String line;
		    try {
				while ((line = m_buffReader.readLine()) != null) {
					line += "\n"; // place the '\n' back, required in ActivitySeeFile to set up table
					stringAL.add(line);
				}
			} catch (IOException e) {
				Toast.makeText(m_context, m_fileName + " not read from internal storage, good luck bud.", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			return stringAL;
		}
		
		// Writes a single line and a new line char to m_file
		// NOTE: Must be called AFTER makeWriteFile() to initialize m_buffWriter
		public void writeLineToFile(String line) {
			if(m_buffWriter == null) {
				makeWriteFile();
			}
			if(m_buffWriter != null) {
				try {
					m_buffWriter.append(String.valueOf(line));
					m_buffWriter.newLine();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(m_context, "writeLineToFile(), Line not written to file." , Toast.LENGTH_LONG).show();
				}
			}
		}
		
		// Returns full path of file
		public String getFullPath() {
			return m_path + m_fileName;
		}
		
		// Attempts to close both m_buffReader and m_buffWriter, does nothing if either is uninitialized
		public void close() {
			try {
				if(m_buffReader != null) {
					m_buffReader.close();
				}
				if(m_buffWriter != null) {
					m_buffWriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(m_context, "close(), A buffer wasn't closed properly.", Toast.LENGTH_LONG).show();
			}
		}
		
		// Deletes m_file
		// NOTE: not currently used, may be buggy
		public boolean delete() {
			return m_file.delete();
		}
	}
		
	/** Internal Storage interactions class */
	
	public class InternalStorageHelper {
		private final String TAG = InternalStorageHelper.class.getSimpleName();
		private String m_fileName;
		private Context m_context;
		private FileInputStream m_fis;
		private FileOutputStream m_fos;
		private String writeString = "";
		
		// NOTE: fileName must have the format: "myFileName.csv"
		public InternalStorageHelper (Context context, String fileName) {
			m_context = context;
			m_fileName = fileName;			
		}
		
		// Checks if m_fileName exists in Internal Storage
		// Note: When this class is used for writing files, this method should be called AFTER makeWriteFile() 
		public boolean fileExists() {
			File file = m_context.getFileStreamPath(m_fileName);
			return FileIOHelper.exists(file);
		}
		
		// Initializes FileInputStream m_fis for reading, call BEFORE any read action
		public void makeReadFile() {
			try {
				m_fis = m_context.openFileInput(m_fileName);
			} catch (FileNotFoundException e) {
				Toast.makeText(m_context, m_fileName + " not opened for read in internal storage, good luck bud.", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
		
		// Initializes FileOutputStream m_fos for writing, call BEFORE any write action
		public void makeWriteFile() {
			try {
				m_fos = m_context.openFileOutput(m_fileName, Context.MODE_PRIVATE);
			} catch (FileNotFoundException e) {
				Toast.makeText(m_context, m_fileName + " not opened for write in internal storage, good luck bud.", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
		
		// Build a string to be written later.
		// NOTE: Supplied parameter "line" must have a '\n' character at the tail
		public void buildWriteString(String line) {
			writeString += line;
		}
		
		// Convert writeString into bytes and writes it into Internal Storage
		// NOTE: Should be called AFTER a string has been built using buildWriteString(line)
		// NOTE: Must be called AFTER makeWriteFile() to initialize m_fos
		public void writeToFile() {
			try {
				m_fos.write(writeString.getBytes());
			} catch (IOException e) {
				Toast.makeText(m_context, m_fileName + " not written to in internal storage, good luck bud.", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
		
		// Reads a file from Internal Storage
		// Returns an ArrayList<String> with each line[i] of file (ending with '\n') as an element at index i. 
		// NOTE: Must be called AFTER makeReadFile() to initialize m_fis
		public ArrayList<String> readFromFile() {
			ArrayList<String> stringAL = new ArrayList<String>();
		    InputStreamReader inputStreamReader = new InputStreamReader(m_fis);
		    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		    String line;
		    try {
				while ((line = bufferedReader.readLine()) != null) {
					stringAL.add(line);
				}
			} catch (IOException e) {
				Toast.makeText(m_context, m_fileName + " not read from internal storage, good luck bud.", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			return stringAL;
		}
		
		// Reads a file from Internal Storage, same as readFromFile() EXCEPT
		// Returns an ArrayList<Float>
		public ArrayList<Float> readFromFileFloats() {
			ArrayList<Float> floatAL = new ArrayList<Float>();
		    InputStreamReader inputStreamReader = new InputStreamReader(m_fis);
		    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		    String line;
		    try {
				while ((line = bufferedReader.readLine()) != null) {
					try {
						floatAL.add(Float.parseFloat(line));
					} catch (NumberFormatException e) {
						if(BuildConfig.DEBUG) Log.e(TAG, e.toString());
					}
				}
			} catch (IOException e) {
				Toast.makeText(m_context, m_fileName + " not read from internal storage, good luck bud.", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			return floatAL;
		}
		
		// Attempts to close both m_fis and m_fos streams, does nothing if either is not initialized
		public void close() {
			try {
				if(m_fis != null) {
					m_fis.close();
				}
				if(m_fos != null) {
					m_fos.close();
				}
			} catch (IOException e) {
				Toast.makeText(m_context, "FileOutputStream not closed. Good luck.", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
		
	}
	
	// Checks if file exists, returns false if file is uninitialized
	private static boolean exists(File file) {
		if(file != null) {
			return file.exists();
		} else {
			return false;
		}
	}
	
	// Check if External Storage is accessible, pulled from Android developers site
	private static boolean checkExternalStorageState() {
		// Check if External Storage is accessible
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		return mExternalStorageAvailable && mExternalStorageWriteable;
	}
}