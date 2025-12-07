package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.ChatMessageDto;
import com.Tsimur.Dubcast.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(
        name = "Chat",
        description = "Public chat history endpoints. " +
                "Messages are returned in chronological order (oldest first) inside each response."
)
public class ChatRestController {

    private final MessageService messageService;

    @GetMapping("/messages")
    @Operation(
            summary = "Get last chat messages",
            description = """
                    Returns the last N chat messages as a flat list.

                    Messages are ordered from oldest to newest inside the response.
                    This endpoint is typically used on initial page load to show recent chat activity.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of chat messages successfully returned",
            content = @Content(
                    array = @ArraySchema(
                            schema = @Schema(implementation = ChatMessageDto.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid limit parameter",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Unexpected server error",
            content = @Content
    )
    public List<ChatMessageDto> getMessages(
            @Parameter(
                    description = "Maximum number of messages to return. " +
                            "If not specified, defaults to 50. " +
                            "Values <= 0 or > 200 are normalized on the server side.",
                    example = "50"
            )
            @RequestParam(defaultValue = "50") int limit
    ) {
        return messageService.getLastMessages(limit);
    }

    @GetMapping("/messages/page")
    @Operation(
            summary = "Get paged chat history",
            description = """
                    Returns a page of chat messages for infinite scroll / lazy loading.

                    Messages inside each page are ordered from oldest to newest.
                    Page index is zero-based (page=0 is the most recent page).
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "A page of chat messages successfully returned",
            content = @Content(
                    array = @ArraySchema(
                            schema = @Schema(implementation = ChatMessageDto.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid page or size parameters",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Unexpected server error",
            content = @Content
    )
    public List<ChatMessageDto> page(
            @Parameter(
                    description = "Zero-based page index. 0 = latest messages page.",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Page size (number of messages per page). " +
                            "If not specified, defaults to 50. " +
                            "Values <= 0 or > 200 are normalized on the server side.",
                    example = "50"
            )
            @RequestParam(defaultValue = "50") int size
    ) {
        return messageService.getMessagesPage(page, size);
    }
}
