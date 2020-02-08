package ru.java.mentor.oldranger.club.restcontroller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.java.mentor.oldranger.club.dto.TopicAndCommentsDTO;
import ru.java.mentor.oldranger.club.model.media.Photo;
import ru.java.mentor.oldranger.club.model.user.User;
import ru.java.mentor.oldranger.club.service.media.PhotoAlbumService;
import ru.java.mentor.oldranger.club.service.media.PhotoService;
import ru.java.mentor.oldranger.club.service.utils.SecurityUtilsService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/securedPhoto")
@Tag(name = "Secured photos")
public class SecurePhoto {


    @NonNull
    private SecurityUtilsService securityUtilsService;
    @NonNull
    private PhotoService photoService;
    @NonNull
    private PhotoAlbumService photoAlbumService;

    @Value("${photoalbums.location}")
    private String albumsdDir;

    @Operation(security = @SecurityRequirement(name = "security"),
            summary = "Return photo as byte array from album", tags = {"Topic and comments"})
    @Parameter(in = ParameterIn.QUERY, name = "type",
            required = false, description = "размер картинки (необязательный параметр)",
            allowEmptyValue = false,
            schema = @Schema(
                    type = "String",
                    example = "http://localhost:8888/api/securedPhoto/photoFromAlbum/1?type=small"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo found",
                    content = @Content(schema = @Schema(implementation = Array.class))),
            @ApiResponse(responseCode = "400", description = "Secure or id error")})
    @GetMapping(value = "/photoFromAlbum/{photoId}")
    public ResponseEntity<byte[]> getAlbumPhoto(@PathVariable(value = "photoId") Long photoId,
                                                @RequestParam(value = "type", required = false) String type) throws IOException {
        User currentUser = securityUtilsService.getLoggedUser();
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        if(type == null) {
            type = "original";
        }
        Photo photo = photoService.findById(photoId);
        Set<User> viewers = photo.getAlbum().getViewers();
        if (viewers.size() != 0 && !viewers.contains(currentUser)) {
            return ResponseEntity.badRequest().build();
        }
        String path = "";
        if(type.equals("original")) {
            path = photo.getOriginal();
        }
        if(type.equals("small")) {
            path = photo.getSmall();
        }
        return ResponseEntity.ok(IOUtils.toByteArray(new FileInputStream(new File(albumsdDir + File.separator + path))));
    }
}