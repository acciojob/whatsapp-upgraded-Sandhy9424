package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;
@Repository
public class WhatsappRepository {
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 1;
        this.messageId = 1;
    }
    public String createUser(String name, String mobile) throws Exception {
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        userMobile.add(mobile);
        return "SUCCESS";
    }
    public Group createGroup(List<User> users){
        if(users.size()==2){
            Group t=new Group(users.get(1).getName(),users.size());
            adminMap.put(t,users.get(0));
            groupUserMap.put(t,users);
            return t;
        }
        else{
            String s="Group"+" "+customGroupCount;
            Group t=new Group(s,users.size());
            adminMap.put(t,users.get(0));
            groupUserMap.put(t,users);
            customGroupCount++;
            return t;
        }
    }
    public int createMessage(String content){
        Message message=new Message(messageId,content);
        messageId++;
        return message.getId();
    }
    public int sendMessage(Message message, User sender, Group group) throws Exception{
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        senderMap.put(message,sender);
        List<User>t=new ArrayList<>(groupUserMap.get(group));
        for(User x:t) {
            if (x.getMobile().equals(sender.getMobile())){
                if (groupMessageMap.containsKey(group)) {
                    groupMessageMap.get(group).add(message);
                    return groupMessageMap.get(group).size();
                } else {
                    List<Message> m = new ArrayList<>();
                    m.add(message);
                    groupMessageMap.put(group, m);
                    return m.size();
                }
            }
        }
        throw new Exception("You are not allowed to send message");
    }
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        if(!approver.getMobile().equals(adminMap.get(group).getMobile())){
            throw new Exception("Approver does not have rights");
        }
        List<User>t=new ArrayList<>(groupUserMap.get(group));
        for(User x:t) {
            if(x.getMobile().equals(user.getMobile())){
                adminMap.put(group,user);
                return "SUCCESS";
            }
        }
        throw new Exception("User is not a participant");
    }
    public int removeUser(User user) throws Exception{
        for(Group x:groupUserMap.keySet()) {
            List<User> t = new ArrayList<>(groupUserMap.get(x));
            for (User f : t) {
                if (f.getMobile().equals(user.getMobile())) {
                    if (adminMap.get(x).getMobile().equals(user.getMobile())) {
                        throw new Exception("Cannot remove admin");
                    } else {
                        List<Message> m = new ArrayList<>(groupMessageMap.get(x));
                        for (Message g : m) {
                            if (senderMap.get(g).getMobile().equals(user.getMobile())) {
                                m.remove(g);
                                senderMap.remove(g);
                            }
                        }
                        groupUserMap.get(x).remove(user);
                        x.setNumberOfParticipants(x.getNumberOfParticipants() - 1);
                        return groupUserMap.get(x).size() + groupMessageMap.get(x).size() + senderMap.size();
                    }
                }
            }
        }
        throw new Exception("User not found");
    }
    public String findMessage(Date start, Date end, int K) throws Exception{
        List<Message>j=new ArrayList<>();
        for (Message m:senderMap.keySet()){
            if(m.getTimestamp().compareTo(start)>0&&m.getTimestamp().compareTo(end)<0){
                j.add(m);
            }
        }
        Collections.sort(j,(a,b)->a.getTimestamp().compareTo(b.getTimestamp()));
        if(K>j.size()){
            throw new Exception("K is greater than the number of messages");
        }
        return j.get(K-1).getContent();
    }
}

