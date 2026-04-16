use serde::{Deserialize, Serialize};

#[derive(Serialize)]
struct Request {
    model: String,
    max_tokens: u32,
    messages: Vec<Message>,
}

#[derive(Serialize)]
struct Message {
    role: String,
    content: String,
}

#[derive(Deserialize)]
struct Response {
    content: Vec<ContentBlock>,
}

#[derive(Deserialize)]
struct ContentBlock {
    #[serde(rename = "type")]
    block_type: String,
    text: Option<String>,
}

#[tokio::main]
async fn main() {
    let api_key = std::env::var("ANTHROPIC_API_KEY").expect("Missing ANTHROPIC_API_KEY on env");

    let client = reqwest::Client::new();

    let body = Request {
        model: "claude-sonnet-4-2025-0514".into(),
        max_tokens: 256,
        messages: vec![Message {
            role: "user".into(),
            content: "In a simple phrase: What is an agent?".into()
        }],
    };

    print!("Calling Api...");

    let resp = client
        .post("https://api.anthropic.com/v1/messages")
        .header("x-api-key", &api_key)
        .header("anthropic-version", "2023-06-01")
        .json(&body)
        .send()
        .await
        .expect("Http Failure");

    if !resp.status().is_success() { 
        let status = resp.status();
        let err = resp.text().await.unwrap_or_default();

        eprint!("API [{status}]: {err}");
        return;
    }

    let data: Response = resp.json().await.expect("Invalid Json");

    let text: String = data
        .content
        .iter()
        .filter_map(|b| b.text.as_ref())
        .cloned()
        .collect::<Vec<_>>()
        .join("");

    println!("{text}")
}