package cat.iesesteveterradas;

public class Question implements Comparable<Question> {
    String id, postTypeId, acceptedAnswerId, creationDate, score, viewCount, body, ownerUserId, lastActivityDate, title, tags, answerCount, commentCount, contentLicense;

    public Question(String id, String postTypeId, String acceptedAnswerId, String creationDate, String score, String viewCount, String body, String ownerUserId, String lastActivityDate, String title, String tags, String answerCount, String commentCount, String contentLicense) {
        this.id = id;
        this.postTypeId = postTypeId;
        this.acceptedAnswerId = acceptedAnswerId;
        this.creationDate = creationDate;
        this.score = score;
        this.viewCount = viewCount;
        this.body = body;
        this.ownerUserId = ownerUserId;
        this.lastActivityDate = lastActivityDate;
        this.title = title;
        this.tags = tags;
        this.answerCount = answerCount;
        this.commentCount = commentCount;
        this.contentLicense = contentLicense;
    }

    @Override
    public int compareTo(Question q) {
        return Integer.compare(Integer.parseInt(q.viewCount), Integer.parseInt(this.viewCount));
    }
}
