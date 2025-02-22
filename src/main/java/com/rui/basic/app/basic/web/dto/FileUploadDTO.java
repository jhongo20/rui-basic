package com.rui.basic.app.basic.web.dto;

import lombok.Data;

@Data
public class FileUploadDTO {
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private byte[] content;
}
