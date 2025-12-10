// 메시지 박스
const chatBox = document.getElementById("chat-box");
const input = document.getElementById("chat-input");
const btn = document.getElementById("send-btn");

// 자동 스크롤
function scrollToBottom() {
    chatBox.scrollTop = chatBox.scrollHeight;
}

// 메시지 출력
function addMessage(text, type) {
	const wrapper = document.createElement("div");
	wrapper.classList.add("message-wrapper", type);
	
	const icon = document.createElement("div");
	icon.classList.add("chat-icon");
	
	//말풍선
    const msg = document.createElement("div");
    msg.classList.add("message", type);
    msg.innerText = text;
	
	if (type === "bot") {
		icon.innerHTML = "🐻";
		wrapper.appendChild(icon);
		wrapper.appendChild(msg);
	} else {
		icon.innerHTML = "🐱";
		wrapper.appendChild(msg);
		wrapper.appendChild(icon);
	}
	
    chatBox.appendChild(wrapper);
    scrollToBottom();
}

// 추천 메뉴 카드 출력
function addMenuCards(menus) {
    const wrapper = document.createElement("div");
    wrapper.classList.add("menu-card-container");

    menus.forEach(m => {
        const card = document.createElement("div");
        card.classList.add("menu-card");

        card.innerHTML = `
            <img src="${m.menuImage}" />
            <div class="desc-overlay">${m.menuDescription ?? "설명 없음"}</div>
        `;

        wrapper.appendChild(card);
    });

    chatBox.appendChild(wrapper);
    scrollToBottom();
}

// REST API 호출
async function sendMessage() {
    const text = input.value.trim();
    if (!text) return;

    addMessage(text, "user");
    input.value = "";

    const response = await fetch("/api/ai/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: text })
    });

    const result = await response.json();

    // AI 메시지 출력
    addMessage(result.answer, "bot");

    // 메뉴 추천이 포함되어 있으면 카드로 렌더링
    if (result.recommendList) {
        addMenuCards(result.recommendList);
    }
}

btn.addEventListener("click", sendMessage);
input.addEventListener("keydown", e => {
    if (e.key === "Enter") sendMessage();
});
