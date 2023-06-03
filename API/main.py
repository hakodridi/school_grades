from fastapi import FastAPI, UploadFile, File, Form
from pydantic import BaseModel
import firebase_admin
from firebase_admin import auth
from firebase_admin import credentials
from firebase_admin import db
from firebase_admin import exceptions as firebase_exceptions
import pandas as pd
import os
from pprint import pprint
from firebase_admin import messaging

# Initialize Firebase
cred = credentials.Certificate("./affichage-2d87a-firebase-adminsdk-q8ift-72f086273a.json")
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://affichage-2d87a-default-rtdb.firebaseio.com/'
})

app = FastAPI()

class UserCreate(BaseModel):
    fullname: str
    username: str
    password: str
    user_type: int
    depart_key: str
    speciality_key: str
    section_key: str
    group: int

@app.get("/")
def read_root():
    return {"message":"test"}

def train_excel_contents(file_path):
    try:
        df = pd.read_excel(file_path)
        dict_list = df.to_dict(orient="records")
        pprint(dict_list)
        for item in dict_list:
            username = item["username"]
            fullname = item["fullname"]
        return {"message": "File uploaded successfully", "error":""}
    except Exception as e:
        print(f"Error reading Excel file: {e}")
        return {"message":"", "error" : "Error reading Excel file"}

@app.post("/upload_profs")
async def upload_file(file: UploadFile = File(...), depart_key: str = Form(...), section_key: str = Form(...), group: str = Form(...) ):
    file_extension = file.filename.split(".")[-1]
    allowed_extensions = ["xlsx", "csv"]
    print(depart_key)
    print(section_key)

    if file_extension not in allowed_extensions:
        return {"message":"", "error": "Invalid file format. Only Excel (xlsx) and CSV files are allowed."}

    

    # Create the temporary directory if it doesn't exist
    os.makedirs("temp", exist_ok=True)

    # Save the file temporarily
    file_path = f"temp/{file.filename}"
    with open(file_path, "wb") as f:
        contents = await file.read()
        f.write(contents)

    # Print the contents of the Excel file
    return train_excel_contents(file_path)

@app.post("/create_student")
def create_student(user: UserCreate):
    try:
        # Create account with Firebase Authentication
        userAuth = auth.create_user(
            email=user.username+"@univ-biskra.dz",
            password=user.password,
            display_name=user.fullname,
            email_verified=False
        )
        
        # Save user data to the Firebase Realtime Database
        user_data = {
            'fullname': user.fullname,
            'username': user.username,
            'user_type': user.user_type,
            'depart_key': user.depart_key,
            'section_key': user.section_key,
            'speciality_key': user.speciality_key,
            'group': user.group
        }
        db.reference('users/students').child(user.depart_key).child(user.section_key).child(str(user.group)).child(userAuth.uid).set(user_data)
        db.reference('users/data').child(userAuth.uid).set(user_data)
        return {"message": "User created successfully", "error":""}
    except ValueError as e:
        print(f"error : {e}")
        return {"error": f"Failed to create user\n{e}", "message":""}

    except firebase_exceptions.FirebaseError as e:
        print(f"error_2 : {e}")
        return {"error": f"Failed to save user data\n{e}", "message":""}
    

@app.post("/create_prof")
def create_prof(user: UserCreate):
    try:
        # Create account with Firebase Authentication
        userAuth = auth.create_user(
            email=user.username+"@univ-biskra.dz",
            password=user.password,
            display_name=user.fullname,
            email_verified=False
        )
        
        # Save user data to the Firebase Realtime Database
        user_data = {
            'fullname': user.fullname,
            'username': user.username,
            'user_type': user.user_type,
            'depart_key': user.depart_key,
            'section_key': user.section_key,
            'group': user.group
        }
        db.reference('users/profs').child(user.depart_key).child(userAuth.uid).set(user_data)
        db.reference('users/data').child(userAuth.uid).set(user_data)
        print("created")
        return {"message": "User created successfully", "error":""}
    except ValueError as e:
        print(f"error : {e}")
        return {"error": f"Failed to create user\n{e}", "message":""}

    except firebase_exceptions.FirebaseError as e:
        print(f"error_2 : {e}")
        return {"error": f"Failed to save user data\n{e}", "message":""}


@app.post("/create_scolarity_user")
def create_scolarity_user(user: UserCreate):
    try:
        # Create account with Firebase Authentication
        userAuth = auth.create_user(
            email=user.username+"@univ-biskra.dz",
            password=user.password,
            display_name=user.fullname,
            email_verified=False
        )
        
        # Save user data to the Firebase Realtime Database
        user_data = {
            'fullname': user.fullname,
            'username': user.username,
            'user_type': user.user_type,
            'depart_key': user.depart_key,
            'section_key': user.section_key,
            'group': user.group
        }

        db.reference('scolarity').child(user.depart_key).set(userAuth.uid)
        db.reference('users/data').child(userAuth.uid).set(user_data)

        print("created")
        return {"message": "User created successfully", "error":""}
    except ValueError as e:
        print(f"error : {e}")
        return {"error": f"Failed to create user\n{e}", "message":""}

    except firebase_exceptions.FirebaseError as e:
        print(f"error_2 : {e}")
        return {"error": f"Failed to save user data\n{e}", "message":""}





def retrieve_tokens(depart_key):
    ref = db.reference('tokens').child(depart_key)
    tokens_snapshot = ref.get()

    # Extract the tokens from the snapshot
    tokens = []
    if tokens_snapshot:
        tokens = list(tokens_snapshot.values())

    return tokens



def send_notification_to_tokens(tokens, title_, body_):
    message = messaging.MulticastMessage(
        notification=messaging.Notification(
            title=title_,
            body=body_
        ),
        tokens=tokens,
    )
    response = messaging.send_multicast(message)
    print('Notification sent:', response.success_count, 'successful')



@app.post("/push_notification/")
async def push_notification(notification_data: dict):
    # Retrieve all the tokens
    tokens = retrieve_tokens(notification_data.get("depart_key"))

    title = notification_data.get('title')
    body = notification_data.get('body')
    pprint(notification_data)

    # Send push notifications to the tokens
    send_notification_to_tokens(tokens, title, body)

    return {"message" : "sent"}

    
    
