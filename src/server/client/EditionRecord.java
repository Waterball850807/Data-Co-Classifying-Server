package server.client;

import java.util.List;

import model.ActivityTag;

public class EditionRecord {
	private List<EditionRecord.Item> items;

	
	public EditionRecord(List<EditionRecord.Item> items) {
		this.items = items;
	}
	
	public List<EditionRecord.Item> getItems() {
		return items;
	}
	
	public void addOrUpdateItem(Item item){
		for (Item it : items) {
			if (it.user.equals(item.user))
			{
				it.tags = item.tags;
				return;
			}
		}
		items.add(item);
	}
	
	
	public boolean containsUser(User user){
		return getItems().stream().anyMatch(i -> i.user.equals(user));
	}
	
	public static class Item{
		public User user;
		public ActivityTag[] tags;
		public Item(User user, ActivityTag[] tags) {
			this.user = user;
			this.tags = tags;
		}
		
	}
}
