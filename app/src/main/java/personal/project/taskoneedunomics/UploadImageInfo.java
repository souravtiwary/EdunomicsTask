package personal.project.taskoneedunomics;

public class UploadImageInfo {
    String title;
    String image;
    String description;
    String search;

    public UploadImageInfo() {
    }

    public UploadImageInfo(String title, String image, String description, String search) {
        this.title = title;
        this.image = image;
        this.description = description;
        this.search = search;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public String getSearch() {
        return search;
    }
}
