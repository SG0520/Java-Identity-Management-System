package com.jwt.identity.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jwt.identity.dto.UserDto;
import com.jwt.identity.repository.UsersRepository;

@Service
public class XlsxHelper {

	@Autowired
	private UsersRepository usersRepository;
	public static String TYPE = "text/csv";

	static String[] HEADERs = { "FirstName", "LastName", "Email", "Mobile", "UserRole", "Password" };

//	public static boolean hasCSVFormat(MultipartFile file) {
//		String fileName = file.getOriginalFilename();
//		if (TYPE.equals(file.getContentType()) || file.getContentType().equals("application/vnd.ms-excel")
//				|| fileName.endsWith(".xlsx")) {
//			return true;
//		}
//		return false;
//	}

	public boolean hasXlsxFormat(MultipartFile file) {
		String fileName = file.getOriginalFilename();
		if (fileName.endsWith(".xlsx")) {
			return true;
		}
		return false;
	}

	/*
	 * public static List<UserDto> csvToEmployeeResource(InputStream is, Integer
	 * tenantId) { try (BufferedReader fileReader = new BufferedReader(new
	 * InputStreamReader(is, "UTF-8")); CSVParser csvParser = new
	 * CSVParser(fileReader,
	 * CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim()
	 * );) { List<UserDto> userObjlist = new ArrayList<>();
	 * 
	 * System.out.println("flow is here"); Iterable<CSVRecord> csvRecords =
	 * csvParser.getRecords();
	 * 
	 * for (CSVRecord csvRecord : csvRecords) { UserDto userObj = new UserDto();
	 * userObj.setFirstName(csvRecord.get("FirstName"));
	 * userObj.setLastName(csvRecord.get("LastName"));
	 * userObj.setEmail(csvRecord.get("Email"));
	 * userObj.setTelephone(csvRecord.get("Mobile"));
	 * userObj.setRole(csvRecord.get("UserRole"));
	 * userObj.setPassword(csvRecord.get("password")); if
	 * (usersRepositorty.findUserByEmailAndTenantId(userObj.getEmail(), tenantId) !=
	 * null) continue; userObjlist.add(userObj); System.out.println(userObj); }
	 * System.out.print(userObjlist);
	 * 
	 * return userObjlist; } catch (IOException e) { throw new
	 * RuntimeException("fail to parse CSV file: " + e.getMessage()); } }
	 */

	public List<UserDto> excelToUserDtoList(InputStream is, Integer tenantId) {
		try (Workbook workbook = WorkbookFactory.create(is)) {
			Sheet sheet = workbook.getSheetAt(0);
			// Assuming the data is in the first sheet
			List<UserDto> userObjList = new ArrayList<>();
			DataFormatter dataFormatter = new DataFormatter();

			int lastRowNum = sheet.getLastRowNum();
			int startRow = 1; // Assuming the first row is the header row, so we start from the second row
			for (int rowNum = startRow; rowNum <= lastRowNum; rowNum++) {
				int latestDataRow = 0;
				Row row = sheet.getRow(rowNum);
				boolean isRowIncomplete = false;
				for (int j = 0; j < row.getLastCellNum(); j++) {
					Cell cell = row.getCell(j);
					if (cell == null || cell.getCellType() == CellType.BLANK) {
						isRowIncomplete = true;
						break; // Exit the inner loop when an empty field is found
					}
				}
				if (isRowIncomplete) {
					continue;
				}
				if (row == null || row.getCell(0) == null || row.getCell(0).getCellType() == CellType.BLANK) {
					boolean hasNextData = false;
					for (int nextRowNum = rowNum + 1; nextRowNum <= lastRowNum; nextRowNum++) {
						Row nextRow = sheet.getRow(nextRowNum);
						if (nextRow != null && nextRow.getCell(0) != null
								&& nextRow.getCell(0).getCellType() != CellType.BLANK) {
							hasNextData = true;
							latestDataRow = nextRowNum;
							break;
						}
					}
					if (rowNum != latestDataRow) {
						continue;
					}
					if (!hasNextData) {
						break; // Exit the loop when encountering an empty row and there is no more data after it			
					}
				}
				UserDto userObj = new UserDto();
				userObj.setFirstName(row.getCell(0).getStringCellValue());
				userObj.setLastName(row.getCell(1).getStringCellValue());
				userObj.setEmail(row.getCell(2).getStringCellValue());
				userObj.setTelephone(dataFormatter.formatCellValue(row.getCell(3)));
				userObj.setRoleName(row.getCell(4).getStringCellValue());
				userObj.setPassword(row.getCell(5).getStringCellValue());
				if (usersRepository.findUserByEmailAndTenantId(userObj.getEmail(), tenantId) == null) {
					userObjList.add(userObj);
				}
			}
			return userObjList;
		} catch (IOException e) {
			throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
		}
	}

}