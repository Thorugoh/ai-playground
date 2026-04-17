use serde::{Deserialize, Serialize};

#[derive(Serialize)]
struct Request {
    contents: Vec<Content>,
}

#[derive(Serialize, Deserialize)]
struct Content {
    role: String,
    parts: Vec<Part>,
}

#[derive(Serialize, Deserialize)]
struct Part {
    text: String
}

#[derive(Deserialize)]
struct Response {
    candidates: Option<Vec<Candidate>>,
}

#[derive(Deserialize)]
struct Candidate {
    content: Content,
}

#[tokio::main]
async fn main() {
    let api_key = std::env::var("GEMINI_API_KEY")
        .expect("Missing GEMINI_API_KEY");

    let model = "gemini-2.0-flash-lite";

    let url = format!(
        "https://generativelanguage.googleapis.com/v1beta/models/{}:generateContent?key={}",
        model, api_key
    );

    let body = Request {
        contents: vec![Content {
            role: "user".into(),
            parts: vec![Part { 
                text: "In a short phrase: what is an AI Agent?".into(),
            }]
        }],
    };

    println!("Calling Gemini...");

    let client = reqwest::Client::new();
    let resp = client
        .post(&url)
        .header("content-type", "application/json")
        .json(&body)
        .send()
        .await
        .expect("Http Failure.");

    let raw = resp.text().await.expect("failed reading body");
    println!("Raw:\n{raw}\n");

    let data: Response = serde_json::from_str(&raw).expect("JSON inválido");

    let text = data
        .candidates
        .as_ref()
        .and_then(|c| c.first())
        .and_then(|c| c.content.parts.first())
        .map(|p| p.text.as_str())
        .unwrap_or("no answer");

    print!("Gemini: {text}")

}