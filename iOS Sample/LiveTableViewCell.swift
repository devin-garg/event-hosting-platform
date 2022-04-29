//
//  LiveTableViewCell.swift
//  StreamSelfLive
//
//  Created by Devin Garg on 8/3/20.
//  Copyright © 2020 Devin Garg. All rights reserved.
//

import UIKit
internal protocol LiveTableViewCellDelegate: NSObjectProtocol {
    func leftrightClicked(id:String, left:Bool)
}
class LiveTableViewCell: UITableViewCell {
    internal weak var delegate: LiveTableViewCellDelegate?
    @IBOutlet var usernameImageView: UIImageView!
    
    @IBOutlet var statusLiveLabel: UILabel!
    @IBOutlet var statusLiveView: UIView!
    @IBOutlet var usernameLabel: UILabel!
    
    @IBOutlet var statsView: UIView!
    
    @IBOutlet var priceHeader: UILabel!
    @IBOutlet var priceLable: UILabel!
    
    @IBOutlet var durationLabel: UILabel!
    @IBOutlet var durationHeader: UILabel!
    
    @IBOutlet var leftImageBtn: UIButton!
    @IBAction func leftImageAction(_ sender: Any) {
        delegate?.leftrightClicked(id: pathIsOn, left: true)
    }
    @IBOutlet var rightImageBtn: UIButton!
    @IBAction func rightImageAction(_ sender: Any) {
        delegate?.leftrightClicked(id: pathIsOn, left: false)
    }
    @IBOutlet var imageCountLabel: UILabel!
    @IBOutlet var multImageStackVIewHeight: NSLayoutConstraint!
    
    @IBOutlet var imageMediaUrl: UIImageView!
    @IBOutlet var imageMediaHeight: NSLayoutConstraint!
    
    @IBOutlet var descriptionLabel: UILabel!
    @IBOutlet var titleLabel: UILabel!
    var pathIsOn:String!
    
    func thePathIAmOn(path:String){
        pathIsOn = path
    }
    
}
