import pandas as pd
import numpy as np
from sklearn.linear_model import LinearRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import r2_score
import matplotlib.pyplot as plt

file_path = 'training_data2.csv'
data = pd.read_csv(file_path)

#commmand to know if the phone is rest or not
data['I_Smooth'] = data['Current_mA'].rolling(window=30).mean()
data['Interaction'] = data['Temperature_C'] * data['I_Smooth']
data['Temp_Sq'] = data['Temperature_C'] ** 2

PREDICTION_TIME = 60
data['Future_Temp_Raw'] = data['Temperature_C'].shift(-PREDICTION_TIME)
data['Future_Temp_Smooth'] = data['Future_Temp_Raw'].rolling(window=15).mean()


data = data.dropna()

#tranning 
X = data[['Temperature_C', 'I_Smooth', 'Interaction', 'Temp_Sq']]
y = data['Future_Temp_Smooth'] # this make the line smoother to increase the exact of result

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

model = LinearRegression()
model.fit(X_train, y_train)

#result
predictions = model.predict(X_test)
accuracy = r2_score(y_test, predictions)

print("="*40)
print(f"ĐỘ CHÍNH XÁC (POLYNOMIAL): {accuracy * 100:.2f}%")
print("="*40)

coef = model.coef_
intercept = model.intercept_

print("\n--- COPY 5 SỐ NÀY VÀO ANDROID (CHÍNH XÁC TỪNG SỐ 0) ---")
print(f"W_TEMP (Nhiệt):        {coef[0]:.10f}")
print(f"W_CURRENT (Dòng điện): {coef[1]:.10f}")
print(f"W_INTERACT (Tương tác):{coef[2]:.10f}")
print(f"W_TEMP_SQ (Nhiệt^2):   {coef[3]:.10f}")
print(f"BIAS (Hệ số bù):       {intercept:.10f}")
print("-------------------------------------------------------")

#Graph
plt.figure(figsize=(12, 6))
plt.plot(data['Future_Temp_Raw'].tail(150).values, label='Thực tế (Nhiễu)', color='lightgray')
plt.plot(y_test.values[:150], label='Thực tế', color='blue')
plt.plot(predictions[:150], label='AI Dự đoán', color='red', linestyle='--')
plt.legend()
plt.title(f'AI Advanced Model (Accuracy: {accuracy*100:.1f}%)')
plt.show()