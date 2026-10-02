package pdf.rag.chat;

import java.util.List;

interface ChatFacade {
    List<MessageDto> getMessages();

    void sendMessage(MessageForm form);
    void clearMessages();
}
