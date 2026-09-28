import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics import r2_score
from sklearn.ensemble import RandomForestRegressor
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType

# 1. Load dữ liệu
file_path = r'training_data7.csv' 
data = pd.read_csv(file_path)

# 2. Kỹ nghệ đặc trưng
data['I_Abs'] = data['Current_mA'].abs()
data['I_Smooth'] = data['I_Abs'].rolling(window=50).mean()

data['Temp_Diff'] = data['Temperature_C'].diff().fillna(0)
data['Temp_Slope'] = data['Temp_Diff'].rolling(window=20).mean()

data['Interaction'] = data['Temperature_C'] * data['I_Smooth']
data['Temp_Sq'] = data['Temperature_C'] ** 2

# 3. Thiết lập mục tiêu
PREDICTION_TIME = 60 
data['Future_Temp_Actual'] = data['Temperature_C'].shift(-PREDICTION_TIME)
data['Future_Temp_Smooth'] = data['Future_Temp_Actual'].rolling(window=20).mean()

data = data.dropna()

# 4. Trích xuất Input và Output
features = ['Temperature_C', 'I_Smooth', 'Interaction', 'Temp_Sq', 'Audio_State', 'Temp_Slope']
X = data[features]
y = data['Future_Temp_Smooth']

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# 5. Huấn luyện bằng Scikit-Learn Random Forest (Hoàn toàn bỏ PMML)
print("Đang huấn luyện mô hình Random Forest (100 cây)...")
model = RandomForestRegressor(n_estimators=100, max_depth=10, random_state=42)
model.fit(X_train, y_train)

# 6. Kiểm tra độ chính xác
predictions = model.predict(X_test)
accuracy = r2_score(y_test, predictions)

print("="*50)
print(f"ĐỘ CHÍNH XÁC MÔ HÌNH (ONNX - Random Forest): {accuracy * 100:.2f}%")
print("="*50)

# 7. Chuyển đổi và xuất file ONNX
print("Đang xuất mô hình sang định dạng ONNX...")

# Định nghĩa đầu vào: Có 6 biến, kiểu float
# (Tên 'float_input' này rất quan trọng, lát nữa bên Java/Android sẽ gọi đúng tên này)
initial_type = [('float_input', FloatTensorType([None, 6]))]

# Chuyển đổi sang ONNX
onnx_model = convert_sklearn(model, initial_types=initial_type)

# Lưu thành file
with open("thermal_rf_model.onnx", "wb") as f:
    f.write(onnx_model.SerializeToString())
    

print("🎉 Đã xuất thành công file: thermal_rf_model.onnx")