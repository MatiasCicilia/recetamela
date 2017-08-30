package controllers;

import models.media.Media;
import models.media.json.MediaJson;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;
import services.MediaService;

import java.io.File;
import java.io.IOException;

/**
 * Media controller persists media files and retrieves their metadata.
 */
public class MediaController extends Controller {

    /**
     * Persists an instance of the Media model in the database and the file in disk.
     * The request body must have a 'multipart/form-data' content type and the file
     * must appear as the value of the key 'file'.
     * @return MediaJson that represents the Media model persisted in the database.
     * @throws IOException If the file cannot be written a response with 500 error is returned.
     */
    public Result createMedia() throws IOException {
        final MultipartFormData<File> body = request().body().asMultipartFormData();
        if (body == null) return badRequest();
        final MultipartFormData.FilePart<File> file = body.getFile("file");
        if (file == null) return badRequest();
        final Media media = MediaService.getInstance().save(file);
        final MediaJson mediaJson = new MediaJson(media);
        return ok(Json.toJson(mediaJson));
    }

    /**
     * @param id ID of the requested Media instance.
     * @return MediaJson that represents a Media instance with provided id
     * persisted in the database.
     */
    public Result getMedia(long id) {
        final Media media = MediaService.getInstance().getById(id);
        if (media == null) return notFound();
        final MediaJson mediaJson = new MediaJson(media);
        return ok(Json.toJson(mediaJson));
    }
}