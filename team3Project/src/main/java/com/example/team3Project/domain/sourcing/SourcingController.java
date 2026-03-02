package com.example.team3Project.domain.sourcing;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;




@Controller
public class SourcingController {
    @GetMapping("/")
    public String mainHome() {
        return "Hello World!";
    }
    
    @GetMapping("/upload")
    public String uploadForm() {
        return "uplaod";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        
        if (file.isEmpty()) {
            model.addAttribute("message", "파일 선택해 주세요.");
            return "upload";
        }

        //일단 받았으면, 실제 로직은 여기서 실행이 됨. 
        String fileName = file.getOriginalFilename();
        model.addAttribute("message", fileName + " 파일이 성공적으로 업로드되었습니다.");
        
        return "upload";
    }
    
    
    
    
}
