package pdf.rag.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import pdf.rag.document.DocumentFacade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService implements ChatFacade {
    private final List<MessageDto> messages = new ArrayList<>();
    private final ChatModel chatModel;
    private final DocumentFacade documentFacade;

    private List<MessageDto> getLastMessages() {
        return messages.stream()
                .skip(Math.max(0, messages.size() - 6))
                .toList();
    }

    private Message getSystemMessage(String userPrompt) {
        // Translated the prompt to English
        String systemPrompt = """
                You are a helpful assistant that answers questions.
                Use the information provided in the <documentation> tag to ensure accurate answers.
                Answer concisely but accurately. If the information is not present in the <documentation> tag, state that you cannot answer based on the provided documents.
                <documentation>{documents}</documentation>
                Previous conversation history is provided in the <messages> tag for context: <messages>{messages}</messages>
                """;
        return new SystemPromptTemplate(systemPrompt)
                .createMessage(Map.of(
                        "documents", documentFacade.getSimilarDocuments(userPrompt),
                        "messages", getLastMessages()
                ));
    }

    @Override
    public List<MessageDto> getMessages() {
        return messages;
    }
    @Override
    public void clearMessages() {
        messages.clear();
    }

    @Override
    public void sendMessage(MessageForm form) {
        Message userMessage = new UserMessage(form.content());
        Message systemMessage = getSystemMessage(form.content());
        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));

        messages.add(new MessageDto(form.content(), MessageType.USER, LocalDateTime.now()));

        String result = chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();

        messages.add(new MessageDto(result, MessageType.SYSTEM, LocalDateTime.now()));
    }
}
