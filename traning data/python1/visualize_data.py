import pandas as pd
import matplotlib.pyplot as plt


file_path = 'training_data5.csv'

try:
    data = pd.read_csv(file_path)
    
    #test data if it works ot not
    print("Dữ liệu mẫu:")
    print(data.head())

    # Analysing data
    # Remove "MB free" tp get number data only
    #data['RAM_Free_MB'] = data['RAM_Free_MB'].astype(str).str.replace(' MB Free', '').astype(int)

    # create figure
    plt.figure(figsize=(12, 6))

    #Create figure red line displays temperature
    plt.subplot(2, 1, 1) 
    plt.plot(data['Temperature_C'], color='red', label='Temperature (°C)')
    plt.title('Biểu đồ Nhiệt độ theo Thời gian')
    plt.ylabel('Temp (°C)')
    plt.legend()
    plt.grid(True)

    #Create figure blue line displays power xomsumption
    plt.subplot(2, 1, 2)
    plt.plot(data['Current_mA'], color='blue', label='Power Consumption (mA)')
    plt.title('Biểu đồ Tiêu thụ Điện năng')
    plt.xlabel('Thời gian (giây)')
    plt.ylabel('Current (mA)')
    plt.legend()
    plt.grid(True)

    #Display both figures
    plt.tight_layout()
    plt.show()

except FileNotFoundError:
    print(f"Lỗi: Không tìm thấy file '{file_path}'")
except Exception as e:
    print(f"Có lỗi xảy ra: {e}")

