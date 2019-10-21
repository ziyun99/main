package seedu.duke.email.entity;

import seedu.duke.email.EmailList;

import java.util.ArrayList;
import java.util.HashMap;

public class EmailTags {

    HashMap<String, SubTagMap> tagMap = new HashMap<>();

    public EmailTags() {
        tagMap = new HashMap<>();
    }

    public HashMap<String, SubTagMap> updateEmailTagList(EmailList emailList) {
        for (Email email : emailList) {
            ArrayList<Email.Tag> tags = email.getTags();
            for (Email.Tag rootTag : tags) {
                String rootTagName = rootTag.getKeywordPair().getKeyword();
                for (Email.Tag subTag : tags) {
                    String subTagName = subTag.getKeywordPair().getKeyword();

                    EmailList subEmailList = new EmailList();
                    SubTagMap subTagMap = new SubTagMap(subTagName, subEmailList);

                    if (tagMap.containsKey(rootTagName)) {
                        subTagMap = tagMap.get(rootTagName);
                    }
                    if (subTagMap.containsKey(subTagName)) {
                        subEmailList = subTagMap.get(rootTagName);
                    }

                    if (!subEmailList.contains(email)) {
                        subEmailList.add(email);
                        subTagMap.put(subTagName, subEmailList);
                        tagMap.put(rootTagName, subTagMap);
                    }
                }
            }
        }
        return tagMap;
    }

    public static class SubTagMap extends HashMap<String, EmailList> {

        public SubTagMap (String subTagName, EmailList subEmailList) {
        }

    }

}
