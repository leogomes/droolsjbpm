package org.drools.repository;

import java.util.Date;
import java.util.Set;

public class RuleDef extends Persistent {

    private String name;
    private long versionNumber;
    private String content;
    private MetaData metaData;
    private String status;
    private boolean checkedOut;    
    private String checkedOutBy;
    private String versionComment;
    private Set tags;
    private String documentation;
    private Date effectiveDate;
    private Date expiryDate;     
    private Date dateCreated;
    
    public Date getDateCreated(){
        return dateCreated;
    }
    private void setDateCreated(Date dateCreated){
        this.dateCreated = dateCreated;
    }
    
    public RuleDef() {}
    
    /**
     * This is for creating a brand new rule.
     * @param name
     * @param content
     */
    public RuleDef(String name, String content) {
        this.name = name;
        this.content = content;
        this.versionNumber = 1;
        this.head = true;
        this.dateCreated = new Date();
    }
    /**
     * This little cheat tells the repo that this
     * rule is at the head of versions.
     */
    private boolean head;
    
    
    public String getContent(){
        return content;
    }
    public void setContent(String content){
        this.content = content;
    }

    public MetaData getMetaData(){
        return metaData;
    }
    public void setMetaData(MetaData metaData){
        this.metaData = metaData;
    }
    public String getStatus(){
        return status;
    }
    public void setStatus(String status){
        this.status = status;
    }

    public boolean isCheckedOut(){
        return checkedOut;
    }
    public void setCheckedOut(boolean checkedOut){
        this.checkedOut = checkedOut;
    }
    public String getCheckedOutBy(){
        return checkedOutBy;
    }
    public void setCheckedOutBy(String checkOutBy){
        this.checkedOutBy = checkOutBy;
    }
    public boolean isHead(){
        return head;
    }
    public void setHead(boolean isHead){
        this.head = isHead;
    }
    public String getVersionComment(){
        return versionComment;
    }
    public void setVersionComment(String versionComment){
        this.versionComment = versionComment;
    }
    public long getVersionNumber(){
        return this.versionNumber;
    }
    private void setVersionNumber(long versionNumber){
        this.versionNumber = versionNumber;
    }
    public String getDocumentation(){
        return documentation;
    }
    public void setDocumentation(String documentation){
        this.documentation = documentation;
    }
    public Set getTags(){
        return tags;
    }
    public void setTags(Set tags){
        this.tags = tags;
    }
    public Date getEffectiveDate(){
        return effectiveDate;
    }
    public void setEffectiveDate(Date effectiveDate){
        this.effectiveDate = effectiveDate;
    }
    public Date getExpiryDate(){
        return expiryDate;
    }
    public void setExpiryDate(Date expiryDate){
        this.expiryDate = expiryDate;
    }
    public String getName(){
        return name;
    }
    public RuleDef setName(String name){
        this.name = name;
        return this;
    }

    
    
}
