package com.sysadminanywhere.views.management.users;

import com.sysadminanywhere.model.ad.UserEntry;
import com.sysadminanywhere.service.UsersService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import lombok.SneakyThrows;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

@Uses(Upload.class)
public class UpdateUserPhotoDialog extends Dialog {

    private final UsersService usersService;
    private final UserEntry user;

    public UpdateUserPhotoDialog(UsersService usersService, UserEntry userEntry, Runnable updateView) {
        this.usersService = usersService;
        this.user = userEntry;

        setHeaderTitle("Updating user photo");
        setWidth("600px");

        StreamResource resource = new StreamResource("", () -> new ByteArrayInputStream(user.getJpegPhoto()));
        AtomicReference<Image> image = new AtomicReference<>(new Image(resource, ""));
        image.get().setHeight("400px");

        MemoryBuffer buffer = new MemoryBuffer();

        Upload upload = new Upload(buffer);
        upload.setMaxFiles(1);
        upload.setAcceptedFileTypes("image/jpeg", ".jpg");

        Button uploadButton = new Button("Upload photo...");
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        upload.setUploadButton(uploadButton);

        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(errorMessage, 5000,
                    Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        AtomicReference<InputStream> fileData = new AtomicReference<>();

        upload.addSucceededListener(event -> {
            fileData.set(buffer.getInputStream());

            byte[] thumbnailPhoto = resizeImage(buffer.getInputStream(), 96, 96);
            byte[] jpegPhoto = resizeImage(buffer.getInputStream(), 648, 648);

            StreamResource resource2 = new StreamResource("", () -> {
                user.setJpegPhoto(jpegPhoto);
                user.setThumbnailPhoto(thumbnailPhoto);
                return new ByteArrayInputStream(jpegPhoto);
            });
            image.get().setSrc(resource2);
        });

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.add(image.get(), upload);

        add(verticalLayout);

        Button saveButton = new com.vaadin.flow.component.button.Button("Save", e -> {
            UserEntry entry = user;

            try {
                usersService.update(entry);
                updateView.run();

                Notification notification = Notification.show("User photo updated");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        com.vaadin.flow.component.button.Button cancelButton = new Button("Cancel", e -> close());

        com.vaadin.flow.component.button.Button deleteButton = new Button("Delete", e -> {
            UserEntry entry = user;

            try {
                entry.setJpegPhoto(null);
                entry.setThumbnailPhoto(null);

                usersService.update(entry);
                updateView.run();

                Notification notification = Notification.show("User photo deleted");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            close();
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        getFooter().add(deleteButton);
        getFooter().add(cancelButton);
        getFooter().add(saveButton);

    }

    @SneakyThrows
    byte[] resizeImage(InputStream originalImage, int targetWidth, int targetHeight)  {

        BufferedImage image = ImageIO.read(originalImage);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(image)
                .size(targetWidth, targetHeight)
                .outputFormat("JPEG")
                .outputQuality(1)
                .toOutputStream(outputStream);
        byte[] data = outputStream.toByteArray();

        return data;
    }

}