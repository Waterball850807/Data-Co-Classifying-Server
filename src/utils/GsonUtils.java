package utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import server.client.ActivityRecord;

public class GsonUtils {
	private static Gson gson = new Gson();
	public static Gson getGson(){
		return gson;
	}
	
	public static List<ActivityRecord> parseActivityRecords(String json){
		Type type = new TypeToken<List<ActivityRecord>>(){}.getType();
		List<ActivityRecord> list = gson.fromJson(json, type);
		return list;
	}
	public static <T> List<T> parseList(String json, Class<T> clazz){
		Type type = new TypeToken<List<T>>(){}.getType();
		List<T> list = gson.fromJson(json, type);
		return list;
	}
}
