/*
 * 
 * This class is for accessing, creating and modifying records in a file
 * 
 * */


import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.JOptionPane;

public class RandomFile {
	private RandomAccessFile output;
	private RandomAccessFile input;
	String errorFileProcessing = "Error processing file!";
	String errorWritingFile = "Error writing to file!";
	String errorClosingFile = "Error closing file!";
	RandomAccessEmployeeRecord record;

	// Create new file
	public void createFile(String fileName) {
		RandomAccessFile file = null;

		try // open file for reading and writing
		{
			file = new RandomAccessFile(fileName, "rw");

		} // end try
		catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, errorFileProcessing);
			System.exit(1);
		} // end catch

		finally {
			try {
				if (file != null)
					file.close(); // close file
			} // end try
			catch (IOException ioException) {
				JOptionPane.showMessageDialog(null, errorFileProcessing);
				System.exit(1);
			} // end catch
		} // end finally
	} 

	// Open file for adding or changing records
	public void openWriteFile(String fileName) {
		try {// open file
			output = new RandomAccessFile(fileName, "rw");
		}
		catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, "File does not exist!");
		} 
	} 

	// Close file for adding or changing records
	public void closeWriteFile() {
		try // close file and exit
		{
			if (output != null)
				output.close();
		} 
		catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, errorClosingFile);
			System.exit(1);
		} 
	} 

	public long addRecords(Employee employeeToAdd) {
		Employee newEmployee = employeeToAdd;
		long currentRecordStart = 0;

		try	{ // output values to file
			record = new RandomAccessEmployeeRecord(newEmployee.getEmployeeId(), newEmployee.getPps(),
					newEmployee.getSurname(), newEmployee.getFirstName(), newEmployee.getGender(),
					newEmployee.getDepartment(), newEmployee.getSalary(), newEmployee.getFullTime());

			output.seek(output.length());// Look for proper position
			record.write(output);// Write object to file
			currentRecordStart = output.length();
		} 
		catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, errorWritingFile);
		} 

		return currentRecordStart - RandomAccessEmployeeRecord.SIZE;// Return position where object starts in the file
	}

	// Change details for existing object
	public void changeRecords(Employee newDetails, long byteToStart) {
		long currentRecordStart = byteToStart;
		// object to be written to file
		Employee oldDetails = newDetails;
		try // output values to file
		{
			record = new RandomAccessEmployeeRecord(oldDetails.getEmployeeId(), oldDetails.getPps(),
					oldDetails.getSurname(), oldDetails.getFirstName(), oldDetails.getGender(),
					oldDetails.getDepartment(), oldDetails.getSalary(), oldDetails.getFullTime());

			output.seek(currentRecordStart);// Look for proper position
			record.write(output);// Write object to file
		} 
		catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, errorWritingFile);
		}
	}	

	// Delete existing object
	public void deleteRecords(long byteToStart) {
		long currentRecordStart = byteToStart;


		try // output values to file
		{
			record = new RandomAccessEmployeeRecord();// Create empty object
			output.seek(currentRecordStart);// Look for proper position
			record.write(output);// Replace existing object with empty object
		} 
		catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, errorWritingFile);
		}
	}

	// Open file for reading
	public void openReadFile(String fileName) {
		try // open file
		{
			input = new RandomAccessFile(fileName, "r");
		}
		catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, "File is not supported!");
		}
	}

	public void closeReadFile() {
		try // close file and exit
		{
			if (input != null)
				input.close();
		} 
		catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, errorClosingFile);
			System.exit(1);
		} 
	} 

	// Get position of first record in file
	public long getFirst() {
		long byteToStart = 0;

		try {// try to get file
			input.length();
		}
		catch (IOException ignored) {
		}
		
		return byteToStart;
	}

	// Get position of last record in file
	public long getLast() {
		long byteToStart = 0;

		try {// try to get position of last record
			byteToStart = input.length() - RandomAccessEmployeeRecord.SIZE;
		} 
		catch (IOException ignored) {
		}
		return byteToStart;
	}

	// Get position of next record in file
	public long getNext(long readFrom) {
		long byteToStart = readFrom;

		try {// try to read from file
			input.seek(byteToStart);// Look for proper position in file
			// if next position is end of file go to start of file, else get next position
			if (byteToStart + RandomAccessEmployeeRecord.SIZE == input.length())
				byteToStart = 0;
			else
				byteToStart = byteToStart + RandomAccessEmployeeRecord.SIZE;
		}
		catch (NumberFormatException | IOException ignored) {
		} 
		return byteToStart;
	}

	// Get position of previous record in file
	public long getPrevious(long readFrom) {
		long byteToStart = readFrom;

		try {// try to read from file
			input.seek(byteToStart);// Look for proper position in file
			// if previous position is start of file go to end of file, else get previous position
			if (byteToStart == 0)
				byteToStart = input.length() - RandomAccessEmployeeRecord.SIZE;
			else
				byteToStart = byteToStart - RandomAccessEmployeeRecord.SIZE;
		} 
		catch (NumberFormatException | IOException ignored) {
		} 
		return byteToStart;
	}

	// Get object from file in specified position
	public Employee readRecords(long byteToStart) {
		Employee thisEmployee;
		RandomAccessEmployeeRecord record = new RandomAccessEmployeeRecord();

		try {// try to read file and get record
			input.seek(byteToStart);// Look for proper position in file
			record.read(input);// Read record from file
		}
		catch (IOException ignored) {
		}
		thisEmployee = record;
		return thisEmployee;
	}

	// Check if PPS Number already in use
	public boolean isPpsExist(String pps, long currentByteStart) {
		RandomAccessEmployeeRecord record = new RandomAccessEmployeeRecord();
		boolean ppsExist = false;
		long oldByteStart = currentByteStart;
		long currentByte = 0;

		try {// try to read from file and look for PPS Number
			// Start from start of file and loop until PPS Number is found or search returned to start position
			while (currentByte != input.length() && !ppsExist) {
				//if PPS Number is in position of current object - skip comparison
				if (currentByte != oldByteStart) {
					input.seek(currentByte);// Look for proper position in file
					record.read(input);// Get record from file
					// If PPS Number already exist in other record display message and stop search
					if (record.getPps().trim().equalsIgnoreCase(pps)) {
						ppsExist = true;
						JOptionPane.showMessageDialog(null, "PPS number already exist!");
					}// end if
				}// end if
				currentByte = currentByte + RandomAccessEmployeeRecord.SIZE;
			}// end while
		} // end try
		catch (IOException ignored) {
		}
		return ppsExist;
	}// end isPpsExist

	// Check if any record contains valid ID - greater than 0
	public boolean isSomeoneToDisplay() {
		boolean someoneToDisplay = false;
		long currentByte = 0;
		RandomAccessEmployeeRecord record = new RandomAccessEmployeeRecord();

		try {// try to read from file and look for ID
			// Start from start of file and loop until valid ID is found or search returned to start position
			while (currentByte != input.length() && !someoneToDisplay) {
				input.seek(currentByte);// Look for proper position in file
				record.read(input);// Get record from file
				// If valid ID exist in stop search
				if (record.getEmployeeId() > 0)
					someoneToDisplay = true;
				currentByte = currentByte + RandomAccessEmployeeRecord.SIZE;
			}// end while
		}
		catch (IOException ignored) {
		}

		return someoneToDisplay;
	}// end isSomeoneToDisplay
}
