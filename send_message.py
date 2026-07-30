import requests
import time
import requests

BASE_URL = "http://localhost:8080"

# Separate caches
token_dict = {}     # username -> token
user_id_dict = {}   # username -> userId

COMMON_EMAIL = "helloemail@gmail.com"
COMMON_PASSWORD = "hello123"


def login_and_get_token(username):
    """
    Returns JWT token
    """
    if username in token_dict:
        return token_dict[username]

    url = f"{BASE_URL}/auth/login"

    payload = {
        "username": username,
        "email": COMMON_EMAIL,
        "password": COMMON_PASSWORD
    }

    try:
        response = requests.get(url, json=payload)

        if response.status_code != 200:
            print(f"[ERROR] Login failed for {username}: {response.text}")
            return None

        data = response.json()
        token = data.get("token")

        if not token:
            print(f"[ERROR] Token missing for {username}")
            return None

        token_dict[username] = token
        return token

    except Exception as e:
        print(f"[EXCEPTION] Login error for {username}: {e}")
        return None


def populate_user_ids(token):
    """
    Calls /friends/users and fills user_id_dict
    """
    url = f"{BASE_URL}/friends/users"

    headers = {
        "Authorization": f"Bearer {token}"
    }

    try:
        response = requests.get(url, headers=headers)

        if response.status_code != 200:
            print(f"[ERROR] Fetch users failed: {response.text}")
            return False

        users = response.json()['data']

        # Expected: [{ "userId": 1, "username": "mohit8" }, ...]
        for user in users:
            uname = user.get("username")
            uid = user.get("userId")

            if uname and uid:
                user_id_dict[uname] = uid

        return True

    except Exception as e:
        print(f"[EXCEPTION] Fetch users error: {e}")
        return False


def get_user_credentials(username):
    """
    Returns (userId, token)
    """
    token = login_and_get_token(username)
    if not token:
        return None

    # If userId already known → return
    if username in user_id_dict:
        return user_id_dict[username], token

    # Else populate user IDs
    success = populate_user_ids(token)
    if not success:
        return None

    if username not in user_id_dict:
        print(f"[ERROR] userId not found for {username}")
        return None

    return user_id_dict[username], token
def send_message(token, receiver_id, content):
    """
    Sends a message using JWT token
    """
    url = f"{BASE_URL}/messeges/send"

    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    payload = {
        "receiverId": receiver_id,
        "content": content
    }

    try:
        response = requests.post(url, json=payload, headers=headers)

        if response.status_code != 200:
            print(f"[ERROR] Message send failed: {response.text}")
        else:
            print(f"[SUCCESS] Message sent -> {content}")

    except Exception as e:
        print(f"[EXCEPTION] Send message error: {e}")

def send_friend_request(sender_token, receiver_id):
    url = f"{BASE_URL}/friends/request"

    headers = {
        "Authorization": f"Bearer {sender_token}",
        "Content-Type": "application/json"
    }

    payload = {
        "receiverId": receiver_id
    }

    try:
        response = requests.post(url, json=payload, headers=headers)

        if response.status_code != 200:
            print(f"[ERROR] Friend request failed: {response.text}")
        else:
            print(f"[SUCCESS] Friend request sent")

    except Exception as e:
        print(f"[EXCEPTION] Friend request error: {e}")


def approve_friend_request(receiver_token, sender_id):
    url = f"{BASE_URL}/friends/request/action"

    headers = {
        "Authorization": f"Bearer {receiver_token}",
        "Content-Type": "application/json"
    }

    payload = {
        "senderId": sender_id,
        "status": "APPROVED"
    }

    try:
        response = requests.post(url, json=payload, headers=headers)

        if response.status_code != 200:
            print(f"[ERROR] Friend approve failed: {response.text}")
        else:
            print(f"[SUCCESS] Friend request approved")

    except Exception as e:
        print(f"[EXCEPTION] Friend approve error: {e}")

     import time

def process_conversations(conversations):
    """
    Enhanced flow:
    1. Login users
    2. Send friend request
    3. Approve request
    4. Send messages
    """
    for convo in conversations:
        sender_username = convo["sender"]
        receiver_username = convo["receiver"]

        sender_creds = get_user_credentials(sender_username)
        receiver_creds = get_user_credentials(receiver_username)
        if not sender_creds or not receiver_creds:
            print("[ERROR] Skipping conversation due to missing credentials")
            continue

        sender_id, sender_token = sender_creds
        receiver_id, receiver_token = receiver_creds

        print(f"\n=== Setting up friendship: {sender_username} → {receiver_username} ===")

         # ✅ Step 1: Send friend request
        send_friend_request(sender_token, receiver_id)
        time.sleep(0.2)

         # ✅ Step 2: Approve friend request
        approve_friend_request(receiver_token, sender_id)
        time.sleep(0.2)

        print(f"=== Friendship established ===\n")

         # ✅ Step 3: Send messages
        for msg in convo["messages"]:
            msg_sender = msg["sender"]
            content = msg["content"]

            if msg_sender == 's':
                token = sender_token
                target_id = receiver_id
            else:  # 'r'
                token = receiver_token
                target_id = sender_id

            send_message(token, target_id, content)

            time.sleep(0.2)
# ------------------ SAMPLE USAGE ------------------

if __name__ == "__main__":

    conversations = [

        {
            "sender": "mohit8",
            "receiver": "mohit10",
            "messages": [
                {"sender": "s", "content": "Hey 10"},
                {"sender": "s", "content": "Are you there?"},
                {"sender": "r", "content": "Yes bro"},
                {"sender": "r", "content": "What's up"},
                {"sender": "s", "content": "Nothing much"},
                {"sender": "s", "content": "Working on project"},
                {"sender": "s", "content": "It's tiring"},
                {"sender": "r", "content": "Same here"},
                {"sender": "r", "content": "Backend issues again"},
                {"sender": "s", "content": "JWT?"},
                {"sender": "r", "content": "Yes exactly"},
                {"sender": "s", "content": "I fixed it yesterday"},
                {"sender": "s", "content": "Will send you code"},
                {"sender": "r", "content": "Great"},
                {"sender": "r", "content": "Thanks bro"},
                {"sender": "s", "content": "Anytime"},
            ]
        },

        # 8 ↔ 12
        {
            "sender": "mohit8",
            "receiver": "mohit12",
            "messages": [
                {"sender": "s", "content": "Hey 12"},
                {"sender": "r", "content": "Hello"},
                {"sender": "r", "content": "How are you"},
                {"sender": "s", "content": "Good"},
                {"sender": "s", "content": "Just coding"},
                {"sender": "s", "content": "You?"},
                {"sender": "r", "content": "Watching videos"},
                {"sender": "r", "content": "Learning Spring"},
                {"sender": "s", "content": "Nice"},
                {"sender": "s", "content": "Spring is powerful"},
                {"sender": "r", "content": "Yes but confusing"},
                {"sender": "s", "content": "Start with basics"},
                {"sender": "r", "content": "Will do"},
                {"sender": "r", "content": "Thanks"},
                {"sender": "s", "content": "Ping me anytime"},
                {"sender": "s", "content": "I will help"},
            ]
        },

        # 10 ↔ 11
        {
            "sender": "mohit10",
            "receiver": "mohit11",
            "messages": [
                {"sender": "s", "content": "Bro 11"},
                {"sender": "s", "content": "Need help"},
                {"sender": "r", "content": "Tell me"},
                {"sender": "s", "content": "DB issue"},
                {"sender": "s", "content": "Query slow"},
                {"sender": "r", "content": "Add index"},
                {"sender": "r", "content": "Check explain"},
                {"sender": "s", "content": "Tried that"},
                {"sender": "r", "content": "Then optimize joins"},
                {"sender": "s", "content": "Okay"},
                {"sender": "s", "content": "Will try"},
                {"sender": "r", "content": "Let me know"},
                {"sender": "r", "content": "If still issue"},
                {"sender": "s", "content": "Sure"},
                {"sender": "s", "content": "Thanks"},
                {"sender": "r", "content": "Welcome"},
            ]
        },

        # 9 ↔ 11
        {
            "sender": "mohit9",
            "receiver": "mohit11",
            "messages": [
                {"sender": "s", "content": "Hey"},
                {"sender": "r", "content": "Hi"},
                {"sender": "s", "content": "Free?"},
                {"sender": "s", "content": "Need to discuss"},
                {"sender": "r", "content": "Yes tell"},
                {"sender": "s", "content": "Project idea"},
                {"sender": "r", "content": "Go ahead"},
                {"sender": "s", "content": "Chat app"},
                {"sender": "s", "content": "With real-time"},
                {"sender": "r", "content": "Use WebSocket"},
                {"sender": "r", "content": "Spring supports"},
                {"sender": "s", "content": "Okay"},
                {"sender": "s", "content": "Will explore"},
                {"sender": "r", "content": "Good"},
                {"sender": "r", "content": "Keep me posted"},
                {"sender": "s", "content": "Sure"},
                {"sender": "s", "content": "Thanks"},
            ]
        },

        # 9 ↔ 10
        {
            "sender": "mohit9",
            "receiver": "mohit10",
            "messages": [
                {"sender": "s", "content": "Yo 10"},
                {"sender": "s", "content": "Game tonight?"},
                {"sender": "r", "content": "Yes"},
                {"sender": "r", "content": "What time"},
                {"sender": "s", "content": "10 PM"},
                {"sender": "s", "content": "Be ready"},
                {"sender": "r", "content": "Okay"},
                {"sender": "s", "content": "Invite others"},
                {"sender": "r", "content": "Sure"},
                {"sender": "r", "content": "Will call 11"},
                {"sender": "s", "content": "Great"},
                {"sender": "s", "content": "Let's win"},
                {"sender": "r", "content": "Haha"},
                {"sender": "r", "content": "Yes"},
                {"sender": "s", "content": "See you"},
                {"sender": "s", "content": "Bye"},
            ]
        }

    ]

    process_conversations(conversations)