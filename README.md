# FastAPI Setup Guide with Virtual Environment (venv) and Firebase Integration

This guide will walk you through the steps to set up a FastAPI project using a virtual environment (venv), install dependencies from a `requirements.txt` file, and configure Firebase integration by updating the Firebase Admin URL and certificate.

## Prerequisites

-   Python 3.7 or higher installed on your system.
-   `pip` package manager (usually comes with Python installation).
-   Basic understanding of virtual environments and FastAPI.

## Setup Steps

### 1. Clone the Repository

Clone the repository containing your FastAPI project:
```Shell
$ git clone https://github.com/hakodridi/school_grades.git
$ cd <repository_directory>
```

### 2. Create and Activate a Virtual Environment

Create a virtual environment using `venv` and activate it:

```Shell
$ python3 -m venv venv
$ venv\Scripts\activate  # CMD Windows
```

### 3. Install Project Dependencies

Install the project dependencies specified in the `requirements.txt` file:


`$ pip install -r requirements.txt` 

### 4. Update Firebase Admin URL and Certificate

Navigate to the directory containing your FastAPI project, and locate the file where the Firebase Admin URL and certificate are configured. Typically, this would be in a file named `main.py` or `firebase.py`.

Open the file in a text editor and find the lines where the Firebase Admin URL and certificate are set. They may look similar to the following:


```python
import firebase_admin
from firebase_admin import credentials

cred = credentials.Certificate('path/to/serviceAccountKey.json')
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://your-project-id.firebaseio.com'
})
```

Update the `'databaseURL'` parameter to your desired Firebase Admin URL and provide the correct path to your Firebase service account JSON file.

### 5. Run the FastAPI Application

Finally, start the FastAPI application by running the following command:

```Shell
$ cd API 
$ uvicorn main:app --reload --host 0.0.0.0
```

This command assumes that your FastAPI application is located in a file named `main.py`. Adjust the command if your main file has a different name.

Once the application is running, you can access it by visiting `http://localhost:8000` in your web browser.