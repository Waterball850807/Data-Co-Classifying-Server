package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JdbcActivityRepository implements ActivityRepository{
	private Connection con;
	
	public void setConnection(Connection con) {
		this.con = con;
	}
	
	@Override
	public Activity createActivity(Activity activity) {
		PreparedStatement st = null;
		try {
			st = con.prepareStatement("INSERT INTO Activities VALUES("
					 + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			st.setString(1, activity.getTitle());
			st.setString(2, Activity.dateToString(activity.getStartDate()));
			st.setString(3, Activity.dateToString(activity.getEndDate()));
			st.setString(4, Activity.dateToString(activity.getUpdatedDate()));
			st.setString(5, activity.getContent());
			st.setString(6, activity.getCategory());
			st.setString(7, activity.getSource());
			st.setString(8, activity.getLink());
			st.setString(9, activity.getAddress());
			st.setString(10, activity.getContact());
			int rowCount = st.executeUpdate();
			activity.setId(rowCount);
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
	          if (st != null) try { st.close(); } catch(Exception e) {}
	    }
		return activity;
	}
	
	@Override
	public Activity getActivity(int id) {
		Activity activity = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT * FROM Activities WHERE id = " + id);
			activity = resultToActivity(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
	          if (st != null) try { st.close(); } catch(Exception e) {}
	          if (rs != null) try { rs.close(); } catch(Exception e) {}
	    }
		return activity;
	}

	@Override
	public List<Activity> getActivities() {
		List<Activity> activities = new ArrayList<>();
		Statement st = null;
		try {
			st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM Activities");
			while(rs.next())
				activities.add(resultToActivity(rs));
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
	          if (st != null) try { st.close(); } catch(Exception e) {}
	    }
		return activities;
	}

	private Activity resultToActivity(ResultSet rs) throws SQLException{
		int id = rs.getInt(1);
		String title = rs.getString(2);
		Date startDate = Activity.parseDate(rs.getString(3));
		Date endDate = Activity.parseDate(rs.getString(4));
		Date updatedDate = Activity.parseDate(rs.getString(5));
		String content = rs.getString(6);
		String category = rs.getString(7);
		String source = rs.getString(8);
		String link = rs.getString(9);
		String address = rs.getString(10);
		String contact = rs.getString(11);
		return new Activity(id, title, startDate, endDate, updatedDate, 
				content, category, source, link, address, contact);
	}
	
	public void attachTagsToActivity(int activityId, int[] tagIds){
		String query = "INSERT INTO ActivityClassifications (activityId, tagId) VALUES ";
		StringBuilder strb = new StringBuilder();
		strb.append(query);
		
		for (int tagId : tagIds)
			strb.append("(").append(activityId).append(",").append(tagId).append(")").append(",");
		
		query = strb.substring(0, strb.length()-1);
		Statement st = null;
		try {
			st = con.createStatement();
			int rowCount = st.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
	          if (st != null) try { st.close(); } catch(Exception e) {}
	    }
	}

	@Override
	public ActivityTag[] getActivityTags() {
		List<ActivityTag> activityTags = new ArrayList<>();
		Statement st = null;
		try {
			st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM ActivityTags");
			while(rs.next())
				activityTags.add(new ActivityTag(rs.getInt(1), rs.getString(2)));
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
	          if (st != null) try { st.close(); } catch(Exception e) {}
	    }
		ActivityTag[] tagNameArray = new ActivityTag[activityTags.size()];
		return activityTags.toArray(tagNameArray);
	}

	@Override
	public ActivityTag getActivityTag(int tagId) {
		ActivityTag activityTag = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT * FROM ActivityTags WHERE id = " + tagId);
			activityTag = new ActivityTag(rs.getInt(1), rs.getString(2));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
	          if (st != null) try { st.close(); } catch(Exception e) {}
	          if (rs != null) try { rs.close(); } catch(Exception e) {}
	    }
		return activityTag;
	}
	
	public static void main(String[] argv){
		//testing program
		JdbcProxy jdbcProxy = new JdbcProxy(new JdbcActivityRepository());
		int[] tagIds = new int[]{1, 2};
		//jdbcProxy.attachTagsToActivity(1, tagIds);
		for (ActivityTag tag : jdbcProxy.getActivityTags())
			System.out.println(tag.getName());
		System.out.println(jdbcProxy.getActivityTag(1).getName());
		System.out.println(jdbcProxy.getActivityTag(2).getName());
	}


}
