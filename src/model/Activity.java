package model;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Activity {
	public static final SimpleDateFormat DATE_FORMAT;
	
	static{
		DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	
	public static Date parseDate(String dateStr){
		try {
			return dateStr == null ? null : DATE_FORMAT.parse(dateStr);
		} catch (ParseException e) {
			throw new RuntimeException("若出現這個錯誤請通知水球更正。", e);
		}
	}
	
	public static String dateToString(Date date){
		return date == null ? null : DATE_FORMAT.format(date);
	}
	
	public Activity(int id, String title, Date startDate, Date endDate, Date updatedDate, String content,
			String source, String link, String address, String contact) {
		this(title, startDate, endDate, updatedDate, contact, source, link, address, contact);
		this.id = id;
	}
	
	public Activity(String title, Date startDate, Date endDate, Date updatedDate, String content,
			String source, String link, String address, String contact) {
		this.title = title;
		this.startDate = startDate;
		this.endDate = endDate;
		this.updatedDate = updatedDate;
		this.content = content;
		this.source = source;
		this.link = link;
		this.address = address;
		this.contact = contact;
	}

	public Activity() {}

	private int id;
	private String title;
	private Date startDate;
	private Date endDate;
	private Date updatedDate;
	private String content;
	private String source;
	private String link;
	private String address;
	private String contact;
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.updatedDate = createdDate;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	@Override
	public String toString() {
		return "Activity [id=" + id + ", title=" + title + ", startDate=" + startDate + ", endDate=" + endDate
				+ ", updatedDate=" + updatedDate + ", content=" + content + ", source="
				+ source + ", link=" + link + ", address=" + address + ", contact=" + contact + "]";
	}
}
