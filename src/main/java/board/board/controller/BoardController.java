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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import board.board.dto.BoardDto;
import board.board.dto.BoardFileDto;
import board.board.service.BoardService;

// @Slf4j
@Controller
public class BoardController {
	
	@Autowired
	private BoardService boardService;
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@RequestMapping("/board/openBoardList.do")
	public ModelAndView openBoardList() throws Exception {
		
		log.debug("openBoardList");
		
		ModelAndView mv = new ModelAndView("board/boardList");
		
		List<BoardDto> list = boardService.selectBoardList();
		
		mv.addObject("list", list);
		
		return mv;
	}
	
	@RequestMapping("/board/openBoardWrite.do")
	public String openBoardWrite() throws Exception {
		
		return "board/boardWrite";
	}
	
	@RequestMapping("/board/insertBoard.do")
	public String insertBoard(BoardDto board, MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {
		
		boardService.insertBoard(board, multipartHttpServletRequest);
		
		return "redirect:/board/openBoardList.do";
	}
	
	@RequestMapping("/board/openBoardDetail.do")
	public ModelAndView openBoardDetail(@RequestParam int boardIdx, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		ModelAndView mv = new ModelAndView("board/boardDetail");
		
		String checkResult = hitCountCheck(boardIdx, request, response);
		
		BoardDto board = boardService.selectBoardDetail(boardIdx, checkResult);
		
		mv.addObject("board", board);
		
		return mv;
	}
	
	@RequestMapping("/board/updateBoard.do")
	public String updateBoard(BoardDto board, MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {
		
		boardService.updateBoard(board, multipartHttpServletRequest);
		
		return "redirect:/board/openBoardList.do";
	}
	
	@RequestMapping("/board/deleteBoard.do")
	public String deleteBoard(int boardIdx) throws Exception {
		
		boardService.deleteBoard(boardIdx);
		
		return "redirect:/board/openBoardList.do";
	}
	
	@RequestMapping("/board/downloadBoardFile.do")
	public void downloadBoardFile(@RequestParam int idx, @RequestParam int boardIdx, HttpServletResponse response) throws Exception {
		
		BoardFileDto boardFile = boardService.selectBoardFileInformation(idx, boardIdx);
		
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
	
	private String hitCountCheck(int boardIdx, HttpServletRequest request, HttpServletResponse response) {
		
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
