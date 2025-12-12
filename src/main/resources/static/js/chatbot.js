//전역 변수
let sessionId = null;

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

// AI 로딩 말풍선 추가
function addLoadingMessage() {
    const wrapper = document.createElement("div");
    wrapper.classList.add("message-wrapper", "bot");
    wrapper.id = "ai-loading";

    const icon = document.createElement("div");
    icon.classList.add("chat-icon");
    icon.innerHTML = "🐻";

    const msg = document.createElement("div");
    msg.classList.add("message", "bot", "loading");
    msg.innerHTML = `
        <img src="/img/chat/loading.gif" alt="AI 응답 중...">
    `;

    wrapper.appendChild(icon);
    wrapper.appendChild(msg);

    chatBox.appendChild(wrapper);
    scrollToBottom();
}

function replaceLoadingMessage(text) {
    const wrapper = document.getElementById("ai-loading");
    if (!wrapper) return;

    const msg = wrapper.querySelector(".message");
    msg.classList.remove("loading");
    msg.innerText = text;

	requestAnimationFrame(() => {
		msg.classList.add("show");
	});
	
    wrapper.removeAttribute("id");
    scrollToBottom();
}

// REST API 호출
async function sendMessage() {
    const msg = input.value.trim();
    if (!msg || msg === "") return;

	//user 메시지
	addMessage(msg, "user");
	input.value = "";
	
	//로딩 말풍선 먼저
	addLoadingMessage();

	try {
		const res = await fetch("/api/chat/send", {
	        method: "POST",
	        headers: {"Content-Type": "application/json"},
	        body: JSON.stringify({
	            seqSession: sessionId,
	            message: msg
	        })
	    });
		
		const data = await res.json();
		
		//세션 메시지 출력
		sessionId = data.seqSession;
		
		//로딩 말풍선을 AI 응답으로 교체
		replaceLoadingMessage(data.aiMessage ?? "응답 오류", "bot");
		
		//메뉴 추천 UI
		const best = data.recommendList[0];
		if (best) {
			addMenuCard(best);
		}
			
	} catch (err) {
		replaceLoadingMessage("⚠ 오류 발생: " + err, "bot");
	}
}

function addMenuCard(menu) {
	
    const wrapper = document.createElement("div");
    wrapper.classList.add("menu-card-container");

    const card = document.createElement("div");
    card.classList.add("menu-card", "fade-in");

    card.innerHTML = `
		<div class="menu-image">
            <img src="${menu.menuImage}" alt="">
            <div class="desc-overlay">${menu.menuDescription ?? ""}</div>
        </div>

        <div class="menu-info">
            <h3>${menu.menuName}</h3>
            <div class="tag-badges">
                ${menu.allTags
                    .map(t => `<span class="tag-badge">${convertTag(t)}</span>`)
                    .join("")}
            </div>
        </div>
    `;

    wrapper.appendChild(card);
    chatBox.appendChild(wrapper);
    scrollToBottom();
}

function convertTag(tag) {
    const mapping = {
        spicy: "#매콤",
        sweet: "#달달",
        salty: "#짭짤",
        meat: "#고기",
        seafood: "#해산물",
        soup: "#국물요리",
        noodle: "#면요리",
        oily: "#기름진",
        healthy: "#건강식"
    };
    return mapping[tag] ?? tag;
}

btn.addEventListener("click", sendMessage);
input.addEventListener("keydown", e => {
    if (e.key === "Enter") sendMessage();
});
