INSERT INTO job_definition (class_name, name, package_name, groovy_class)
SELECT 'ArchiveFileDataJob', 'Archive File Data Job', 'com.wkelms.ebilling.edsscheduler.job', ''
    WHERE NOT EXISTS (SELECT 1 from job_definition where name = 'Archive File Data Job')


UPDATE job_definition set groovy_class = '
	package com.wkelms.ebilling.edsscheduler.job

	import com.wkelms.ebilling.edsscheduler.service.util.BeanUtil
	import org.quartz.JobExecutionContext
	import org.quartz.JobExecutionException
	import org.slf4j.Logger
	import org.springframework.beans.factory.annotation.Autowired
	import java.sql.Timestamp;
	import com.wkelms.ebilling.edsscheduler.util.dao.impl.EdsMongoDao
	import com.mongodb.BasicDBObject
	
	class ArchiveFileDataJob extends BaseJob {
	
		@Autowired
		BeanUtil bUtil
	
		@Autowired
		EdsMongoDao md
		
		@Override
		void execute(JobExecutionContext jobExecutionContext, Logger logger) throws JobExecutionException {
			logger.info("Executing Archiving file data job");
			def updateEpacket = new HashMap<String, Object>();
			def updateAttachment = new HashMap<String, Object>();
                try {
                def ago = new Timestamp((new Date() -30).getTime())
                def query = new BasicDBObject();
                query.put("uploadDate", new BasicDBObject("\$lt", ago))
                query.put("ePacket", new BasicDBObject("\$ne", ""))
                def rows = md.select("invoice_parcel",query)
                def refIds = []
                rows["invoice_parcel"].each {
                    refIds.add(it["referenceId"])
                }
                logger.info("Archive referenceIds : " + refIds.toString())

                def attachmentQuery = new BasicDBObject();
                attachmentQuery.put("referenceId", new BasicDBObject("\$in", refIds))
                attachmentQuery.put("attachment", new BasicDBObject("\$ne", ""))
                updateAttachment.put("attachment", "");
                def attachmentRows = md.update("invoice_attachment",updateAttachment,attachmentQuery)

                updateEpacket .put("ePacket", "");
                def invoiceParcelRows = md.update("invoice_parcel",updateEpacket,query)

                logger.info( attachmentRows?.get("invoice_attachment") + " Invoice file(s) deleted from invoice attachment");
                logger.info( invoiceParcelRows?.get("invoice_parcel") + " Invoice file(s) deleted from invoice parcel");
            }
			catch(Exception e) {
                logger.error( "Exception in Archiving File Data Job " + e);
            }
			logger.info("Finished Archiving file data job");
			sleep(2000)
		}
	}
' where name = 'Archive File Data Job'

INSERT INTO job_definition (class_name, name, package_name, groovy_class)
SELECT 'ArchiveInvoiceDataJob', 'Archive Invoice Data Job', 'com.wkelms.ebilling.edsscheduler.job', ''
    WHERE NOT EXISTS (SELECT 1 from job_definition where name = 'Archive Invoice Data Job')


UPDATE job_definition set groovy_class = '
	package com.wkelms.ebilling.edsscheduler.job

	import com.wkelms.ebilling.edsscheduler.service.util.BeanUtil
	import org.quartz.JobExecutionContext
	import org.quartz.JobExecutionException
	import org.slf4j.Logger
	import org.springframework.beans.factory.annotation.Autowired
	import java.sql.Timestamp;
	import com.wkelms.ebilling.edsscheduler.util.dao.impl.EdsMongoDao
	import com.wkelms.ebilling.edsscheduler.util.dao.impl.SharedocArceinvDao
	import com.mongodb.BasicDBObject

	class ArchiveFileDataJob extends BaseJob {

		@Autowired
		BeanUtil bUtil

		@Autowired
		EdsMongoDao md

        @Autowired
        SharedocArceinvDao  arceinv

		@Override
		void execute(JobExecutionContext jobExecutionContext, Logger logger) throws JobExecutionException {
			logger.info("Executing Archiving invoice data job");
			def arcAttachmentList = [];
            def arcTransportFilesList = [];
            def refIdList = [];
            try {
                def ago = new Timestamp((new Date() - 120 ).getTime())
                def query = new BasicDBObject();
                query.put("uploadDate", new BasicDBObject("\$lt", ago))
                def rows = md.select("invoice_parcel",query)
                rows["invoice_parcel"].each {
                    def arcAttachment = new HashMap<String, Object>();
                    def arcTransportFiles = new HashMap<String, Object>();
                    def mongoArc = new HashMap<String, Object>();

                    arcAttachment.put("ReferenceID", it["referenceId"]);
                    arcAttachment.put("DocFormat", it["parcelType"]);
                    arcAttachment.put("DocType", "INV");
                    arcAttachment.put("FileName", it["fileName"]);
                    arcAttachment.put("Status", "IMP");
                    arcAttachment.put("FileID", it["fileId"]);
                    arcAttachment.put("UploadDate", it["uploadDate"] == null ? null : it["uploadDate"]?.format("yyyy-MM-dd HH:mm:ss.SSS", TimeZone.getTimeZone("UTC")));
                    arcAttachment.put("Errcode", "0");
                    arcAttachment.put("OutDocFormat", "XML");
                    arcAttachmentList.add(arcAttachment)

                    def status = ""
                    if (it["status"] == "Delivered")
                        status = "DELI"
                    else if (it["status"] == "Pending")
                        status = "PEND"
                    else if (it["status"] == "Wait")
                        status = "WAIT"

                    arcTransportFiles.put("ReferenceID", it["referenceId"]);
                    arcTransportFiles.put("SenderID", it["senderLawId"]);
                    arcTransportFiles.put("recipientid", it["clientLawId"]);
                    arcTransportFiles.put("BrandName", "");
                    arcTransportFiles.put("sent", it["uploadDate"] == null ? null : it["uploadDate"]?.format("yyyy-MM-dd HH:mm:ss.SSS", TimeZone.getTimeZone("UTC")));
                    arcTransportFiles.put("received", it["updatedAt"] == null ? null : it["updatedAt"]?.format("yyyy-MM-dd HH:mm:ss.SSS", TimeZone.getTimeZone("UTC")));
                    arcTransportFiles.put("purged", Date.parseToStringDate(new Date().toString()).format("yyyy-MM-dd hh:mm:ss.SSS"));
                    arcTransportFiles.put("status", status);
                    arcTransportFiles.put("Currencycode", "");
                    arcTransportFiles.put("signed", it["signed"]);
                    arcTransportFiles.put("manual", "false");
                    arcTransportFiles.put("AttachmentCount", it["attachmentCount"]);
                    arcTransportFiles.put("WarningCount", it["warningCount"]);
                    arcTransportFilesList.add(arcTransportFiles)

                    refIdList.add(it["referenceId"])
                }
                def deleteQuery = new BasicDBObject();
                deleteQuery.put("referenceId", new BasicDBObject("\$in", refIdList))

                if (refIdList.size() > 0) {
                    def arcAttachment = arceinv.insert("arc_attachment", arcAttachmentList)
                    def arcTransportFiles = arceinv.insert("arc_transport_files", arcTransportFilesList)

                    def deleteAttachmentRows = md.delete("invoice_attachment", deleteQuery)
                    def deleteInvoiceParcelRows = md.delete("invoice_parcel", deleteQuery)

                    logger.info("Archive referenceIds : " + refIdList?.toString())
                    logger.info(deleteAttachmentRows?.get("invoice_attachment") + " record(s) deleted from invoice attachment");
                    logger.info(deleteInvoiceParcelRows?.get("invoice_parcel") + " records(s) deleted from invoice parcel");
                }
                else {
                    logger.info("Archive referenceIds : " + refIdList?.toString())
                    logger.info("0 record(s) deleted from invoice attachment");
                    logger.info("0 record(s) deleted from invoice parcel");
                }


            }
            catch(Exception e) {
                logger.error( "Exception in Archiving Invoice Data Job " + e);
            }
			logger.info("Finished Archiving invoice data job");
			sleep(2000)
		}
	}
' where name = 'Archive Invoice Data Job'