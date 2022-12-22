# from keras.models import load_model
from PIL import Image, ImageOps
import numpy as np
import cv2
import torch
import clip 

cam = cv2.VideoCapture(0)

# Load the model and the labels
device = "cuda" if torch.cuda.is_available() else "cpu"
model, preprocess = clip.load("ViT-L/14", device=device)
labels = ["Healthy", "Diseased","Withered","Other"]

# Define a function to capture image
def image_capture():
    ret,frame = cam.read()
    cv2.imwrite ("result.png",frame)

def image_detector():
    image = Image.open('result.png')

    # Preprocess the image and generate the embeddings
    text = clip.tokenize(["healthy plant", "diseased plant", "withered plant", "other"]).to(device)
    with torch.no_grad():
        image = preprocess(image).unsqueeze(0).to(device)
        logits_per_image,_ = model(image, text)
        probs = logits_per_image.softmax(dim=-1).cpu().numpy()
        ind = np.argmax(probs)
    return labels[ind]