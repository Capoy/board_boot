package board.common;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import board.board.dto.BoardFileDto;
import board.board.entity.BoardFileEntity;

@Component
public class FileUtils {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public List<BoardFileDto> parseFileInfo(int boardIdx, MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {
		
		if(ObjectUtils.isEmpty(multipartHttpServletRequest)) {
			
			return null;
		}
		
		List<BoardFileDto> fileList = new ArrayList<>();
		
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
		
		ZonedDateTime current = ZonedDateTime.now();
		
		String path = "upload/" + current.format(format);
		
		File file = new File(path);
		
		if(file.exists() == false) {
			
			file.mkdirs();
		}
		
		Iterator<String> iterator = multipartHttpServletRequest.getFileNames();
		
		String originalFileName, newFileName, originalFileExtension, contentType;
		
		while(iterator.hasNext()) {
			
			List<MultipartFile> list = multipartHttpServletRequest.getFiles(iterator.next());
			
			for(MultipartFile multipartFile : list) {
				
				if(multipartFile.isEmpty() == false) {
					
					originalFileName = multipartFile.getOriginalFilename();
					originalFileName = originalFileName.substring(originalFileName.lastIndexOf("\\") + 1);
					
					contentType = multipartFile.getContentType();
					
//					if(ObjectUtils.isEmpty(contentType)) {
//						
//						break;
//						
//					} else {
//						
//						if(contentType.contains("image/jpeg")) {
//							
//							originalFileExtension = ".jpg";	
//							
//						} else if(contentType.contains("image/png")) {
//							
//							originalFileExtension = ".png";
//							
//						} else if(contentType.contains("image/gif")) {
//							
//							originalFileExtension = ".gif";
//							
//						} else {
//							
//							break;
//						}
//					}
					
					newFileName = Long.toString(System.nanoTime()) + "_" + originalFileName;
//					newFileName = Long.toString(System.nanoTime()) + originalFileExtension;
					
					BoardFileDto boardFile = new BoardFileDto();
					boardFile.setBoardIdx(boardIdx);
					boardFile.setFileSize(multipartFile.getSize());
					boardFile.setOriginalFileName(originalFileName);
//					boardFile.setOriginalFileName(multipartFile.getOriginalFilename());
					boardFile.setStoredFilePath(path + "/" + newFileName);
					
					fileList.add(boardFile);
					
					file = new File(path + "/" + newFileName);
					
					multipartFile.transferTo(file);
				}
			}
		}
		
		return fileList;
	}
	
	public List<BoardFileEntity> parseFileInfo(MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {
		
		if(ObjectUtils.isEmpty(multipartHttpServletRequest)) {
			
			return null;
		}
		
		List<BoardFileEntity> fileList = new ArrayList<>();
		
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
		
		ZonedDateTime current = ZonedDateTime.now();
		
		String path = "upload/" + current.format(format);
		
		File file = new File(path);
		
		if(file.exists() == false) {
			
			file.mkdirs();
		}
		
		Iterator<String> iterator = multipartHttpServletRequest.getFileNames();
		
		String originalFileName, newFileName, originalFileExtension, contentType;
		
		while(iterator.hasNext()) {
			
			List<MultipartFile> list = multipartHttpServletRequest.getFiles(iterator.next());
			
			for(MultipartFile multipartFile : list) {
				
				if(multipartFile.isEmpty() == false) {
					
					originalFileName = multipartFile.getOriginalFilename();
					originalFileName = originalFileName.substring(originalFileName.lastIndexOf("\\") + 1);
					
					contentType = multipartFile.getContentType();
					
//					if(ObjectUtils.isEmpty(contentType)) {
//						
//						break;
//						
//					} else {
//						
//						if(contentType.contains("image/jpeg")) {
//							
//							originalFileExtension = ".jpg";	
//							
//						} else if(contentType.contains("image/png")) {
//							
//							originalFileExtension = ".png";
//							
//						} else if(contentType.contains("image/gif")) {
//							
//							originalFileExtension = ".gif";
//							
//						} else {
//							
//							break;
//						}
//					}
					
					newFileName = Long.toString(System.nanoTime()) + "_" + originalFileName;
//					newFileName = Long.toString(System.nanoTime()) + originalFileExtension;
					
					BoardFileEntity boardFile = new BoardFileEntity();
					
					// boardFile.setBoardIdx(boardIdx); @OneToMany 연관관계로 따로 저장 필요 X
					boardFile.setFileSize(multipartFile.getSize());
					boardFile.setOriginalFileName(originalFileName);
//					boardFile.setOriginalFileName(multipartFile.getOriginalFilename());
					boardFile.setStoredFilePath(path + "/" + newFileName);
					boardFile.setCreatorId("admin");
					
					fileList.add(boardFile);
					
					file = new File(path + "/" + newFileName);
					
					multipartFile.transferTo(file);
				}
			}
		}
		
		return fileList;
	}
	
	public void deleteFiles(List<BoardFileDto> list) {
		
		if(list == null || list.size() == 0) {
			
			return;
		}
		
		for(BoardFileDto boardFileDto : list) {
			
			try {
				
				Path file = Paths.get(boardFileDto.getStoredFilePath());
				
				Files.deleteIfExists(file);
				
			} catch (Exception e) {
				
				log.error("delete error: " + e.getMessage());
			}
			
		}
		
	}
	
	public void deleteFilesJpa(List<BoardFileEntity> list) {
		
		if(list == null || list.size() == 0) {
			
			return;
		}
		
		for(BoardFileEntity boardFileEntity : list) {
			
			try {
				
				Path file = Paths.get(boardFileEntity.getStoredFilePath());
				
				Files.deleteIfExists(file);
				
			} catch (Exception e) {
				
				log.error("delete error: " + e.getMessage());
			}
			
		}
		
	}
}
