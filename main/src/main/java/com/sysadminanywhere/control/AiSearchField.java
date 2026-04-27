package com.sysadminanywhere.control;

import com.sysadminanywhere.common.ai.model.AiNaturalSearchRequest;
import com.sysadminanywhere.common.ai.model.AiNaturalSearchResponse;
import com.sysadminanywhere.common.ai.model.AiSearchTranslation;
import com.sysadminanywhere.service.AiAssistantService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AiSearchField extends HorizontalLayout {

    private final TextField searchField;
    private final Button searchButton;
    private final AiAssistantService aiAssistantService;
    private final AiSearchResultHandler resultHandler;
    private final String objectType;

    public AiSearchField(AiAssistantService aiAssistantService, AiSearchResultHandler resultHandler, String objectType) {
        this.aiAssistantService = aiAssistantService;
        this.resultHandler = resultHandler;
        this.objectType = objectType;

        this.searchField = new TextField();
        this.searchField.setPlaceholder("Search in natural language...");
        this.searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        this.searchField.addClassName("ai-search-field");

        this.searchButton = new Button("AI Search");
        this.searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.searchButton.addClickListener(e -> performAiSearch());

        add(searchField, searchButton);
        addClassName(LumoUtility.Gap.SMALL);
        setWidthFull();
    }

    private void performAiSearch() {
        String query = searchField.getValue();
        if (query == null || query.isBlank()) {
            showNotification("Please enter a search query", NotificationVariant.LUMO_ERROR);
            return;
        }

        searchButton.setEnabled(false);
        searchField.setEnabled(false);

        AiNaturalSearchRequest request = AiNaturalSearchRequest.builder()
                .query(query)
                .objectType(objectType)
                .build();

        try {
            AiNaturalSearchResponse response = aiAssistantService.translateNaturalSearch(request);

            if (response.isSuccess() && response.getTranslation() != null) {
                resultHandler.handleResult(response.getTranslation());
                showNotification("AI search applied successfully", NotificationVariant.LUMO_SUCCESS);
            } else {
                String errorMsg = response.getErrorMessage() != null ? response.getErrorMessage() : "AI search failed";
                showNotification(errorMsg, NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception ex) {
            log.error("AI search failed", ex);
            showNotification("AI search failed: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        } finally {
            searchButton.setEnabled(true);
            searchField.setEnabled(true);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
    }

    public String getValue() {
        return searchField.getValue();
    }

    public void clear() {
        searchField.clear();
    }

    @FunctionalInterface
    public interface AiSearchResultHandler {
        void handleResult(AiSearchTranslation translation);
    }
}
