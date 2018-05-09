/*
 
 POC to investigate following:
 
 1) When Jira issue has an assignee change
 2) Check who is the new assignee and email to this person (if person is "marked" one)


Config this script to be as a Jira script listener for event Issue Assigned

May 2018 mika.nokka1@gmail.com 

*/

// TODO clean imports
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.customfields.option.LazyLoadedOption
import org.apache.log4j.Logger
import org.apache.log4j.Level
import com.atlassian.jira.issue.link.IssueLinkTypeManager
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.fields.CustomField
import java.sql.Timestamp;

import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;

import com.atlassian.jira.security.roles.ProjectRoleManager
import com.atlassian.mail.queue.SingleMailQueueItem
import com.atlassian.mail.MailException
import com.atlassian.mail.Email




def userManager=ComponentAccessor.getUserManager()
def mailServerManager = ComponentAccessor.getMailServerManager()
def mailServer = mailServerManager.getDefaultSMTPMailServer()

// CONFIGURATIONS:
def ToBeTracked="mikanokka" // just hardcoded for POC 
// END OF CONFIGURATIONS




// set logging to Jira log
def log = Logger.getLogger("AssigneeListener") // change for customer system
log.setLevel(Level.DEBUG)  // DEBUG INFO
 
log.debug("---------- AssigneeListener started ------------------------------------------------------")



def util = ComponentAccessor.getUserUtil()
whoisthis2=ComponentAccessor.getJiraAuthenticationContext().getUser()
log.debug("Script run as a user: {$whoisthis2}")

log.debug "Something changed in issue: ${issue}"
assignee=issue.assignee
log.debug "Assignee in issue: ${assignee}"
log.debug "Tracked one: ${ToBeTracked}"
auser=userManager.getUserByName(ToBeTracked)
log.debug "Tracked one via Usermanager: ${auser}"

if (assignee==auser) {   
	log.info "HIT: Found tracked person as an Assignee"
	//start email construction
	if (! mailServerManager.getDefaultSMTPMailServer()) {
		log.error("Cannot send mail as no outgoing mail set up.")
	  }
	else{
		eaddress=auser.getEmailAddress()  // TODO check if no email address for user
		def emailBody="BODY-TEXT" // construct message here
		def emailSubject = "SUBJECT-SUBJECT" // construct message title here
	
		/* Create the email message. */
		def emailFormat = "HTML"
		def email = new Email(eaddress)
		email.setFrom(mailServer.getDefaultFrom())
		email.setMimeType(emailFormat == "HTML" ? "text/html" : "text/plain")
		email.setSubject(emailSubject)
		email.setBody(emailBody)
	
	
		// Send out the email 
		try {
				SingleMailQueueItem item = new SingleMailQueueItem(email)
				ComponentAccessor.getMailQueue().addItem(item)
				log.info("Email sent to tracked person: ${eaddress}")
				
			} catch (MailException e) {
				log.error("Error sending email", e)
				UserMessageUtil.error("Error sending email")
			}
	}	
}
else
{
	log.debug "Assignee is not tracked, no actions taken"
}



	




log.debug "-------AssigneeListener stopping-------------------------------"
	
