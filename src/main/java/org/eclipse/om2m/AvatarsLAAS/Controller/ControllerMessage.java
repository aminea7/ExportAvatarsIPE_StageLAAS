package org.eclipse.om2m.AvatarsLAAS.Controller;


import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name = "employee")
@XmlAccessorType(XmlAccessType.NONE)
public class ControllerMessage implements Serializable
{
   private static final long serialVersionUID = 1L;


   @XmlElement
   private String sender;
    
   @XmlElement
   private String content;
    
   @XmlElement
   private String conversationId;
   
   @XmlElement
   private String replyByDate;
    
   public ControllerMessage(String s, String c, String ci, String r) {
       super();
       sender=s;
       content=c;
       conversationId=ci;
       replyByDate=r;
       
   }
    
   public ControllerMessage(){
        
   }

   //Setters and Getters

   @Override
   public String toString() {
       return "Message [sender=" + sender + ", content=" + content
               + ", conv=" + conversationId + ", replyBD=" + replyByDate + "]";
   }
}