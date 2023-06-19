package board.board.controller;

import java.io.File;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import board.board.entity.BoardEntity;
import board.board.entity.BoardFileEntity;
import board.board.service.JpaBoardService;

@Controller
public class JpaBoardController {
	
	@Autowired
	private JpaBoardService jpaBoardService;
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@RequestMapping(value="/jpa/board", method=RequestMethod.GET)
	public ModelAndView openBoardList() throws Exception {
		
		ModelAndView mv = new ModelAndView("board/jpaBoardList");
		
		List<BoardEntity> list = jpaBoardService.selectBoardList();
		
		mv.addObject("list", list);
		
		return mv;
	}
	
	@RequestMapping(value="/jpa/board/write", method=RequestMethod.GET)
	public String openBoardWrite() throws Exception {
		
		return "board/jpaBoardWrite";
	}
	
	@RequestMapping(value="/jpa/board/write", method=RequestMethod.POST)
	public String writeBoard(BoardEntity board, MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {
		
		jpaBoardService.saveBoard(board, multipartHttpServletRequest);
		
		return "redirect:/jpa/board";
	}
	
	@RequestMapping(value="/jpa/board/detail", method=RequestMethod.GET)
	public ModelAndView openBoardDetail(@RequestParam int boardIdx, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		ModelAndView mv = new ModelAndView("board/jpaBoardDetail");
		
		String checkResult = hitCountCheck(boardIdx, request, response);
		
		BoardEntity board = jpaBoardService.selectBoardDetail(boardIdx, checkResult);
		
		mv.addObject("board", board);
		
		return mv;
	}
	
	@RequestMapping(value="/jpa/board/update", method=RequestMethod.POST)
	public String updateBoard(BoardEntity board, MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {
			
		jpaBoardService.updateBoard(board, multipartHttpServletRequest);
		
		return "redirect:/jpa/board";
	}
	
	@RequestMapping(value="/jpa/board/delete", method=RequestMethod.POST)
	public String deleteBoard(int boardIdx) throws Exception {
		
		jpaBoardService.deleteBoard(boardIdx);
		
		return "redirect:/jpa/board";
	}
	
	@RequestMapping(value="/jpa/board/file", method=RequestMethod.GET)
	public void downloadBoardFile(int boardIdx, int idx, HttpServletResponse response) throws Exception {
		
		BoardFileEntity boardFile = jpaBoardService.selectBoardFileInformation(boardIdx, idx);
		
		if(ObjectUtils.isEmpty(boardFile) == false) {
			
			String fileName = boardFile.getOriginalFileName();
			
			File file = new File(boardFile.getStoredFilePath());
			
			byte[] files = FileUtils.readFileToByteArray(file);
			
//			byte[] files = FileUtils.readFileToByteArray(new File(boardFile.getStoredFilePath()));
			
			response.setContentType(Files.probeContentType(file.toPath()));
			response.setContentLength(files.length);
			response.setHeader("Content-Disposition", "attachment; fileName=\"" + URLEncoder.encode(fileName, "UTF-8") + "\";");
			response.setHeader("Content-Transfer-Encoding", "binary");
			
			response.getOutputStream().write(files);
			response.getOutputStream().flush();
			response.getOutputStream().close();
		}
	}
	
	public String hitCountCheck(int boardIdx, HttpServletRequest request, HttpServletResponse response) {
		
		String checkResult;
		
		Cookie getBoardCookie = null;
	    Cookie[] cookies = request.getCookies();
	    
	    if (cookies != null) {
	    	
	        for (Cookie cookie : cookies) {
	        	
	            if (cookie.getName().equals("getBoard")) {
	                
	            	getBoardCookie = cookie;
	            }
	        }
	    }

	    if (getBoardCookie != null) {
	    	
	        if (!getBoardCookie.getValue().contains("[" + Integer.toString(boardIdx) + "]")) {
	        	
	            getBoardCookie.setValue(getBoardCookie.getValue() + "_[" + boardIdx + "]");
	            getBoardCookie.setPath("/");
	            getBoardCookie.setMaxAge(60 * 60 * 24);
	            
	            response.addCookie(getBoardCookie);
	            
	            checkResult = "Y";
	            
	        } else {
	        	
	        	checkResult = "N";
	        }
	        
	    } else {
	    	
	        Cookie cookie = new Cookie("getBoard", "[" + boardIdx + "]");
	        cookie.setPath("/");
	        cookie.setMaxAge(60 * 60 * 24);
	        
	        response.addCookie(cookie);
	        
	        checkResult = "Y";
	    }
		
		return checkResult;
	}
}
