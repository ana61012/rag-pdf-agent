package pdf.rag.chat;

import java.time.LocalDateTime;

record MessageDto(String content, MessageType type, LocalDateTime dateTime) {
}
