from langchain_core.messages import AIMessage, BaseMessage, HumanMessage, SystemMessage, ToolMessage

from kuzhambu_workers.schemas.ai import AiMessage, AiPrompt, MessageRole


def build_langchain_messages(prompt: AiPrompt) -> list[BaseMessage]:
    return [_to_langchain_message(message) for message in prompt.messages]


def _to_langchain_message(message: AiMessage) -> BaseMessage:
    if message.role == MessageRole.SYSTEM:
        return SystemMessage(content=message.content)
    if message.role == MessageRole.USER:
        return HumanMessage(content=message.content)
    if message.role == MessageRole.ASSISTANT:
        return AIMessage(content=message.content)
    if message.role == MessageRole.TOOL:
        return ToolMessage(content=message.content, tool_call_id="worker-tool")
    raise ValueError(f"unsupported message role: {message.role}")
