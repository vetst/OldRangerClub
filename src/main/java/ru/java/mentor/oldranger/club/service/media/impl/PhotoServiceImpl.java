package ru.java.mentor.oldranger.club.service.media.impl;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.java.mentor.oldranger.club.dao.MediaRepository.PhotoRepository;
import ru.java.mentor.oldranger.club.model.media.Photo;
import ru.java.mentor.oldranger.club.model.media.PhotoAlbum;
import ru.java.mentor.oldranger.club.service.media.PhotoAlbumService;
import ru.java.mentor.oldranger.club.service.media.PhotoService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class PhotoServiceImpl implements PhotoService {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoServiceImpl.class);

    private PhotoRepository photoRepository;

    private PhotoAlbumService albumService;

    @Value("${photoalbums.location}")
    private String albumsdDir;

    @Value("${upload.medium}")
    private int medium;

    @Value("${upload.small}")
    private int small;

    @Autowired
    public void setPhotoAlbumService(PhotoAlbumService service) {
        this.albumService = service;
    }

    @Autowired
    public PhotoServiceImpl(PhotoRepository repository) {
        this.photoRepository = repository;
    }

    @Override
    public Photo save(Long albumId, MultipartFile file) {
        LOG.info("Saving photo to album with id = {}", albumId);
        Photo photo = null;
        try {
            PhotoAlbum album = albumService.findById(albumId);
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            String resultFileName = UUID.randomUUID().toString() + StringUtils.cleanPath(file.getOriginalFilename());
            File uploadPath = new File(albumsdDir + File.separator + userName
                    + File.separator + "photo_albums" + File.separator + album.getId() + File.separator + resultFileName);
            if (!uploadPath.exists()) {
                uploadPath.mkdirs();
            }
            Path copyLocation = Paths.get(uploadPath + File.separator + resultFileName);

            Files.copy(file.getInputStream(), copyLocation);


            Thumbnails.of(uploadPath + File.separator + resultFileName)
                    .size(medium, medium)
                    .toFile(uploadPath + File.separator + "small_" + resultFileName);

            photo = new Photo(resultFileName, "small_" + resultFileName);
            photo.setAlbum(album);
            photo = photoRepository.save(photo);
            albumService.update(album);
            LOG.debug("Photo saved");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return photo;
    }

    @Override
    public Photo findById(Long id) {
        LOG.debug("Getting photo with id = {}", id);
        Photo photo = null;
        try {
            photo = photoRepository.findById(id).get();
            LOG.debug("Album returned");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return photo;
    }

    @Override
    public List<Photo> findPhotoByAlbum(PhotoAlbum album) {
        LOG.debug("Getting photos of album {}", album);
        List<Photo> photos = null;
        try {
            photos = photoRepository.findAllByAlbum(album);
            LOG.debug("Returned list of {} photos", photos.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return photos;
    }

    @Override
    public void deletePhoto(Long id) {
        LOG.info("Deleting photo with id = {}", id);
        try {
            Photo photo = findById(id);
            PhotoAlbum album = photo.getAlbum();
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            File file = new File(albumsdDir + File.separator + userName
                    + File.separator + "photo_albums" + File.separator + album.getId() + File.separator + photo.getOriginal());
            FileSystemUtils.deleteRecursively(file);
            photoRepository.delete(photo);
            LOG.debug("Photo deleted");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

    }

    @Override
    public Photo update(Photo photo) {
        LOG.info("Updating photo with id = {}", photo.getId());
        Photo updatedPhoto = null;
        try {
            updatedPhoto = photoRepository.save(photo);
            LOG.debug("Photo saved");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return updatedPhoto;
    }
}
