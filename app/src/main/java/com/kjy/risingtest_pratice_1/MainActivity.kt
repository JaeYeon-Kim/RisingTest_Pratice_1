package com.kjy.risingtest_pratice_1

import android.Manifest
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kjy.risingtest_pratice_1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    // 파이어베이스 스토리지 버킷 주소 설정
    val storage = Firebase.storage("gs://practicefirebase-ea757.appspot.com")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 업로드 버튼 클릭시 permission Launcher 호출
        // 외부 저장소 읽기 권한을 요청
        // 파이어베이스에 업로드
        binding.uploadBtn.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // 서버로 부터 다운로드
        binding.downloadBtn.setOnClickListener {
            downImage("images/temp_1663238456200.jpeg")
        }
    }

    // 이미지 갤러리는 외부 저장소를 사용하기 때문에, 권한을 요청하는 런처 필요
    // 권한이 승인되었을 경우에만 앞에서 선언한 런처를 실행
    val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        // 권한이 승인되었을 경우
        if(isGranted) {
            // 모든 이미지 불러오기
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(baseContext, "외부 저장소 읽기 권한을 승인해야 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()

        }

    }

    // 이미지 갤러리를 불러오는 런처 생성
    val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uploadImage(uri)
    }

    // 이미지를 업로드
    fun uploadImage(uri: Uri?) {
        // 1. 경로 + 사용자ID + 밀리초로 파일 주소 만들기
        val fullPath = makeFilePath("images", "temp", uri!!)

        // 2. 스토리지에 저장할 경로 설정(위에서 조합해 만든 경로)
        val imageRef = storage.getReference(fullPath)

        // 3. 업로드 태스크 생성
        val uploadTask = imageRef.putFile(uri!!)

        // 4. 업로드 실행 및 확인
        // 작업이 실패했을 경우 메세지를 띄워줌
        uploadTask.addOnFailureListener{
            Log.d("스토리지", "실패=>${it.message}")
            // 작업이 성공했을 경우
        }.addOnSuccessListener { taskSnapshot ->
            Log.d("스토리지", "성공 주소=>${fullPath}")     // 경로를 db에 저장 후 사용
        }
    }

    // 전체 경로를 생성하는 함수
    // 경로, 사용자ID, 확장자를 조합해서 만들어준다.
    // 스토리지 안에서 파일명이 중복되는 것을 방지하기 위해서 사용자 ID를 넣어준다.
    // 파이어베이스에 저장되는 경로 생성
    fun makeFilePath(path: String, userId: String, uri:Uri): String {
        // 엘비스 연산자 사용 가져오지 못했을 경우 /none
        val mimeType = contentResolver.getType(uri)?:"/none"    // 마임타입 예) images/jpeg
        // [] = get 지정된 인덱스 요소를 반환함.
        val ext = mimeType.split("/")[1]        // 확장자 예) jpeg
        val timeSuffix = System.currentTimeMillis()         // 시간값 예) 1232131241312
        val filename = "${path}/${userId}_${timeSuffix}.${ext}"     // 완성

        return filename
    }

    // 이미지를 서버로부터 다운로드
    // 전용 Glide로 가져옴
    fun downImage(path: String) {
        // 스토리지 레퍼런스를 연결하고 이미지 uri를 가져옴
        // 성공적으로 받아왔을 경우
        storage.getReference(path).downloadUrl.addOnSuccessListener { uri ->
        Glide.with(this)
            .load(uri)
            .into(binding.downImage)

        // 받아오는것을 실패했을 경우
        }.addOnFailureListener {
            Log.e("스토리지", "에러=>${it.message}")
        }
    }

}