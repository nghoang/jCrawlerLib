/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ngochoang.CrawlerLib;

import java.io.File;

import javax.activation.MimetypesFileTypeMap;

/**
 *
 * @author Hoang
 */
public class PostFileData {

    private String fileName;
    private String fieldName;
    private String fileMime;
    
    public PostFileData(String fdn,String fn)
    {
    	setFileName(fn);
    	fieldName = fdn;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
    	this.fileName = fileName;
    	File f = new File(fileName);
    	fileMime = new MimetypesFileTypeMap().getContentType(f);
    }

    public String getFileMime() {
        return fileMime;
    }

    /**
     * @return the fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
